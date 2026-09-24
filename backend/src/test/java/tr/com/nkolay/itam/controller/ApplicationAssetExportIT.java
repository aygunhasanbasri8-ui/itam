package tr.com.nkolay.itam.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import org.apache.poi.ss.usermodel.Row;
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
@DisplayName("Uygulama/yazılım Excel dışa aktarımı")
class ApplicationAssetExportIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("dosya üretiliyor ve okunuyor")
    void disaAktarilanDosyaOkunuyor() throws Exception {
        String gövde = """
                {"assetName": "Ofis Paketi Export", "vendor": "Microsoft"}
                """;
        mockMvc.perform(post("/api/v1/applications").with(admin())
                        .contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isCreated());

        byte[] xlsx = mockMvc.perform(get("/api/v1/applications/export"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(xlsx))) {
            Row header = workbook.getSheetAt(0).getRow(4);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("ID");
            assertThat(workbook.getSheetAt(0).getRow(5)).isNotNull();
        }
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor admin() {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                .user("test-admin").roles("ADMIN");
    }
}
