package com.itam.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayOutputStream;
import java.util.List;

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
import org.springframework.transaction.annotation.Transactional;

import com.itam.AbstractIntegrationTest;

@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
@Transactional
@DisplayName("Sunucu Excel içe aktarımı")
class ServerImportIT extends AbstractIntegrationTest {

    private static final List<String> HEADER = List.of("Ad", "Hostname", "Ortam", "Sanal mı", "Sorumlu");

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("hatalı satır varsa hiçbiri yazılmıyor, hatalar satır ve kolonla dönüyor")
    void hataliDosyaHicYazilmaz() throws Exception {
        byte[] file = xlsx(
                List.of("Web 01", "ice-01.example.local", "PROD", "Evet", ""),
                List.of("Web 02", "ice-02.example.local", "CANLI", "", ""),
                List.of("Web 03", "ice-01.example.local", "TEST", "", ""),
                List.of("Web 04", "ice-04.example.local", "DEV", "belki", "Olmayan Kişi"));

        mockMvc.perform(upload(file, false))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(4))
                .andExpect(jsonPath("$.validRows").value(1))
                .andExpect(jsonPath("$.created").value(0))
                .andExpect(jsonPath("$.errors[?(@.row == 3 && @.column == 'Ortam')]").exists())
                .andExpect(jsonPath("$.errors[?(@.row == 4 && @.column == 'Hostname')]").exists())
                .andExpect(jsonPath("$.errors[?(@.row == 5 && @.column == 'Sanal mı')]").exists())
                .andExpect(jsonPath("$.errors[?(@.row == 5 && @.column == 'Sorumlu')]").exists());

        mockMvc.perform(get("/api/v1/servers").param("hostname", "ice-0"))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("önce kontrol, sonra aynı dosyayla yazma; kayıtlı hostname'e ikinci kez izin yok")
    void kontrolSonraYaz() throws Exception {
        byte[] file = xlsx(
                List.of("Db 01", "aktar-01.example.local", "prod", "Evet", ""),
                List.of("Db 02", "aktar-02.example.local", "TEST", "Hayır", ""));

        mockMvc.perform(upload(file, true))
                .andExpect(jsonPath("$.dryRun").value(true))
                .andExpect(jsonPath("$.validRows").value(2))
                .andExpect(jsonPath("$.created").value(0));
        mockMvc.perform(get("/api/v1/servers").param("hostname", "aktar-0"))
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(upload(file, false))
                .andExpect(jsonPath("$.created").value(2))
                .andExpect(jsonPath("$.errors").isEmpty());
        mockMvc.perform(get("/api/v1/servers").param("hostname", "aktar-0"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[?(@.hostname == 'aktar-01.example.local')].virtual").value(true));

        mockMvc.perform(upload(file, true))
                .andExpect(jsonPath("$.validRows").value(0))
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    @Test
    @DisplayName("dışa aktarılan dosya başlık bloğuna rağmen okunuyor")
    void exportDosyasiTaniniyor() throws Exception {
        mockMvc.perform(post("/api/v1/servers").contentType(MediaType.APPLICATION_JSON).content("""
                {"assetName": "geri-01", "hostname": "geri-01.example.local", "environment": "UAT"}
                """)).andExpect(status().isCreated());
        byte[] exported = mockMvc.perform(get("/api/v1/servers/export").param("hostname", "geri-01"))
                .andReturn().getResponse().getContentAsByteArray();

        // Aynı kayıt zaten var: başlık bulunup satır okunduğu için çakışma hatası bekleniyor
        mockMvc.perform(upload(exported, true))
                .andExpect(jsonPath("$.totalRows").value(1))
                .andExpect(jsonPath("$.errors[0].message").value("Bu hostname'le kayıtlı bir sunucu zaten var"));
    }

    @Test
    @DisplayName("Excel olmayan dosya ve USER rolü reddediliyor, şablon indiriliyor")
    void gecersizDosyaVeYetki() throws Exception {
        mockMvc.perform(upload("merhaba".getBytes(), true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors[0].message").exists());
        mockMvc.perform(get("/api/v1/servers/import/template"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "okuyucu", roles = "USER")
    @DisplayName("USER rolü içe aktaramaz")
    void userRoluIceAktaramaz() throws Exception {
        mockMvc.perform(upload(xlsx(), true)).andExpect(status().isForbidden());
    }

    private static org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder upload(
            byte[] content, boolean dryRun) {
        return (org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder)
                multipart("/api/v1/servers/import")
                        .file(new MockMultipartFile("file", "sunucular.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content))
                        .param("dryRun", String.valueOf(dryRun));
    }

    @SafeVarargs
    private static byte[] xlsx(List<String>... rows) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet();
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADER.size(); i++) {
                header.createCell(i).setCellValue(HEADER.get(i));
            }
            for (int r = 0; r < rows.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < rows[r].size(); c++) {
                    row.createCell(c).setCellValue(rows[r].get(c));
                }
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
