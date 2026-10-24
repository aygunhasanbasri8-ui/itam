package com.itam.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.itam.AbstractIntegrationTest;

/** Sunucu dışındaki tipler; ortak akış ServerImportIT'te ayrıntılı sınanıyor. */
@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
@Transactional
@DisplayName("Diğer asset tiplerinin Excel içe aktarımı")
class AssetImportIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("iş istasyonu: departman ve kişi adla bulunuyor, seri no tekrarı reddediliyor")
    void isIstasyonu() throws Exception {
        mockMvc.perform(post("/api/v1/departments").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Aktarım Muhasebe\", \"code\": \"AKT-MUH\", \"active\": true}")).andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\": \"Aktarım Kişisi\", \"code\": \"AKT-01\", \"active\": true}")).andExpect(status().isCreated());

        List<String> header = List.of("Ad", "Seri No", "RAM (GB)", "Zimmetli Kişi", "Departman");
        byte[] file = xlsx(header,
                List.of("Dizüstü 1", "akt-sn-001", "16", "Aktarım Kişisi", "Aktarım Muhasebe"),
                List.of("Dizüstü 2", "AKT-SN-002", "32.0", "AKT-01", "akt-muh"));

        mockMvc.perform(upload("/api/v1/workstations/import", file, false))
                .andExpect(jsonPath("$.created").value(2));
        mockMvc.perform(get("/api/v1/workstations").param("serialNumber", "akt-sn-00"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[?(@.serialNumber == 'AKT-SN-002')].ramGb").value(32))
                .andExpect(jsonPath("$.content[?(@.serialNumber == 'akt-sn-001')].department.name")
                        .value("Aktarım Muhasebe"));

        // Seri numarası büyük/küçük harf farkıyla da aynı sayılıyor (indeks upper() üzerinde)
        byte[] again = xlsx(header,
                List.of("Dizüstü 3", "AKT-SN-001", "on altı", "", "Olmayan Departman"),
                List.of("Dizüstü 4", "yeni-sn", "", "", ""),
                List.of("Dizüstü 5", "YENI-SN", "", "", ""));
        mockMvc.perform(upload("/api/v1/workstations/import", again, true))
                .andExpect(jsonPath("$.validRows").value(1))
                .andExpect(jsonPath("$.errors[?(@.row == 2 && @.column == 'Seri No')].message")
                        .value("Bu seri numarasıyla kayıtlı bir iş istasyonu zaten var"))
                .andExpect(jsonPath("$.errors[?(@.row == 2 && @.column == 'RAM (GB)')]").exists())
                .andExpect(jsonPath("$.errors[?(@.row == 2 && @.column == 'Departman')]").exists())
                .andExpect(jsonPath("$.errors[?(@.row == 4 && @.column == 'Seri No')].message")
                        .value("Aynı seri no dosyada 3. satırda da var"));
    }

    @Test
    @DisplayName("uygulama: tarih hücresi ve metin tarih okunuyor, lisans adedi sayı olmalı")
    void uygulama() throws Exception {
        List<String> header = List.of("Ad", "Tedarikçi", "Lisans Adedi", "Bitiş Tarihi");
        byte[] file;
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet();
            writeRow(sheet.createRow(0), header);
            writeRow(sheet.createRow(1), List.of("Ofis paketi", "aktarim-vendor", "25", ""));
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd.mm.yyyy"));
            sheet.getRow(1).getCell(3).setCellValue(LocalDate.of(2027, 1, 31));
            sheet.getRow(1).getCell(3).setCellStyle(dateStyle);
            writeRow(sheet.createRow(2), List.of("Tasarım aracı", "aktarim-vendor", "", "15.03.2027"));
            workbook.write(out);
            file = out.toByteArray();
        }

        mockMvc.perform(upload("/api/v1/applications/import", file, false))
                .andExpect(jsonPath("$.created").value(2));
        mockMvc.perform(get("/api/v1/applications").param("vendor", "aktarim-vendor"))
                .andExpect(jsonPath("$.content[?(@.assetName == 'Ofis paketi')].expiryDate").value("2027-01-31"))
                .andExpect(jsonPath("$.content[?(@.assetName == 'Ofis paketi')].licenseCount").value(25))
                .andExpect(jsonPath("$.content[?(@.assetName == 'Tasarım aracı')].expiryDate").value("2027-03-15"));

        byte[] bad = xlsx(header, List.of("Hatalı", "", "-3", "31/01/2027"));
        mockMvc.perform(upload("/api/v1/applications/import", bad, true))
                .andExpect(jsonPath("$.errors[?(@.column == 'Bitiş Tarihi')]").exists())
                // @Positive ihlali de dosyadaki başlıkla raporlanıyor
                .andExpect(jsonPath("$.errors[?(@.column == 'Lisans Adedi')]").exists());
    }

    @Test
    @DisplayName("bulut ve iş servisi: enum değerleri harf duyarsız, geçersiz değer listeyle raporlanıyor")
    void bulutVeIsServisi() throws Exception {
        byte[] clouds = xlsx(List.of("Ad", "Sağlayıcı", "Bölge"),
                List.of("Rapor VM", "aws", "eu-central-1"),
                List.of("Yedek", "ORACLE", ""));
        mockMvc.perform(upload("/api/v1/cloud-assets/import", clouds, true))
                .andExpect(jsonPath("$.validRows").value(1))
                .andExpect(jsonPath("$.errors[0].message").value("Geçersiz sağlayıcı: ORACLE (AWS, AZURE, GCP, OTHER)"));

        // Sağlayıcı zorunlu: kolon yoksa başlık satırı tanınmıyor
        mockMvc.perform(upload("/api/v1/cloud-assets/import", xlsx(List.of("Ad"), List.of("x")), true))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("Başlık satırı bulunamadı; en az Ad ve Sağlayıcı kolonları olmalı"));

        byte[] services = xlsx(List.of("Ad", "Kritiklik", "SLA", "Servis Sorumlusu"),
                List.of("Aktarım ödeme", "high", "99.9", ""));
        mockMvc.perform(upload("/api/v1/business-services/import", services, false))
                .andExpect(jsonPath("$.created").value(1));
    }

    @Test
    @DisplayName("başka tipin dosyası tanınmayan kolonlarla reddediliyor, şablonlar iniyor")
    void yanlisTipVeSablon() throws Exception {
        byte[] servers = xlsx(List.of("Ad", "Hostname", "Ortam"), List.of("Web", "w.example.local", "PROD"));
        mockMvc.perform(upload("/api/v1/applications/import", servers, true))
                .andExpect(jsonPath("$.errors[?(@.column == 'Hostname')].message").value("Tanınmayan kolon"));

        for (String type : List.of("applications", "workstations", "cloud-assets", "business-services")) {
            mockMvc.perform(get("/api/v1/" + type + "/import/template")).andExpect(status().isOk());
        }
    }

    @Test
    @WithMockUser(username = "okuyucu", roles = "USER")
    @DisplayName("USER rolü içe aktaramaz")
    void userRoluIceAktaramaz() throws Exception {
        mockMvc.perform(upload("/api/v1/workstations/import", xlsx(List.of("Ad")), true))
                .andExpect(status().isForbidden());
    }

    private static MockMultipartHttpServletRequestBuilder upload(String path, byte[] content, boolean dryRun) {
        return (MockMultipartHttpServletRequestBuilder) multipart(path)
                .file(new MockMultipartFile("file", "aktarim.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content))
                .param("dryRun", String.valueOf(dryRun));
    }

    @SafeVarargs
    private static byte[] xlsx(List<String> header, List<String>... rows) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet();
            writeRow(sheet.createRow(0), header);
            for (int r = 0; r < rows.length; r++) {
                writeRow(sheet.createRow(r + 1), rows[r]);
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private static void writeRow(Row row, List<String> values) {
        for (int c = 0; c < values.size(); c++) {
            row.createCell(c).setCellValue(values.get(c));
        }
    }
}
