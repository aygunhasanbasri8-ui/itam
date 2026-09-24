package tr.com.nkolay.itam.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.AbstractIntegrationTest;

@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
@Transactional
@DisplayName("Uygulama/yazılım CRUD")
class ApplicationAssetControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("kayıt oluşturulabiliyor, süresi yaklaşan lisans filtresiyle bulunabiliyor")
    void olusturVeSuresiYaklasaniBul() throws Exception {
        String gövde = """
                {"assetName": "Adobe Acrobat", "vendor": "Adobe", "licenseType": "Yıllık",
                 "licenseCount": 10, "expiryDate": "2026-10-01"}
                """;

        mockMvc.perform(post("/api/v1/applications").contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vendor").value("Adobe"));

        mockMvc.perform(get("/api/v1/applications").param("expiryBefore", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/api/v1/applications").param("expiryBefore", "2026-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("assetName boşsa 400 döner")
    void assetNameBossaReddedilir() throws Exception {
        String gövde = """
                {"vendor": "Microsoft"}
                """;

        mockMvc.perform(post("/api/v1/applications").contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'assetName')]").exists());
    }

    @Test
    @DisplayName("olmayan uygulama id'si 404 döner")
    void olmayanKayit404() throws Exception {
        mockMvc.perform(get("/api/v1/applications/{id}", 987654))
                .andExpect(status().isNotFound());
    }
}
