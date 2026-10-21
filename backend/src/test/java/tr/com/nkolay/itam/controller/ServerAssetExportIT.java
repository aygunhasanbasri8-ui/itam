package tr.com.nkolay.itam.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.AbstractIntegrationTest;

@AutoConfigureMockMvc
@WithMockUser(username = "export-eden", roles = "USER")
@Transactional
@DisplayName("Sunucu Excel dışa aktarımı")
class ServerAssetExportIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("USER rolü de dışa aktarabiliyor, dosyada başlık bloğu ve veri satırı var")
    void disaAktarilanDosyaOkunuyor() throws Exception {
        sunucuOlustur("export-01.nkolay.local");

        byte[] xlsx = mockMvc.perform(get("/api/v1/servers/export"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(xlsx))) {
            Sheet sheet = workbook.getSheetAt(0);

            Row meta = sheet.getRow(1);
            assertThat(meta.getCell(0).getStringCellValue()).contains("export-eden");

            Row header = sheet.getRow(4);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("ID");

            Row data = sheet.getRow(5);
            assertThat(data).isNotNull();
        }
    }

    @Test
    @DisplayName("seçili kolonlarla dışa aktarılınca sadece o kolonlar yazılıyor")
    void seciliKolonlarlaDisaAktar() throws Exception {
        sunucuOlustur("export-02.nkolay.local");

        byte[] xlsx = mockMvc.perform(get("/api/v1/servers/export").param("columns", "hostname,environment"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(xlsx))) {
            Row header = workbook.getSheetAt(0).getRow(4);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Hostname");
            assertThat(header.getCell(1).getStringCellValue()).isEqualTo("Ortam");
            assertThat(header.getCell(2)).isNull();
        }
    }

    @Test
    @DisplayName("seçilebilecek kolonlar kod ve başlıklarıyla listeleniyor")
    void kolonlarListeleniyor() throws Exception {
        mockMvc.perform(get("/api/v1/servers/export/columns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'hostname')].header").value("Hostname"));
    }

    @Test
    @DisplayName("bilinmeyen kolon kodu 400 döner")
    void bilinmeyenKolonReddedilir() throws Exception {
        mockMvc.perform(get("/api/v1/servers/export").param("columns", "olmayan-kolon"))
                .andExpect(status().isBadRequest());
    }

    private void sunucuOlustur(String hostname) throws Exception {
        String gövde = """
                {"assetName": "%s", "hostname": "%s", "environment": "PROD"}
                """.formatted(hostname, hostname);
        mockMvc.perform(post("/api/v1/servers").with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isCreated());
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor adminAuth() {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                .user("test-admin").roles("ADMIN");
    }
}
