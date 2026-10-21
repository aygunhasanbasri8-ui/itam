package com.itam.controller;

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

import com.itam.AbstractIntegrationTest;

@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
@Transactional
@DisplayName("Cloud varlığı CRUD")
class CloudAssetControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("sağlayıcı belirtilmeden 400 döner")
    void saglayiciBossaReddedilir() throws Exception {
        String gövde = """
                {"assetName": "isimsiz-cloud-varligi"}
                """;

        mockMvc.perform(post("/api/v1/cloud-assets").contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'cloudProvider')]").exists());
    }

    @Test
    @DisplayName("kayıt oluşturulup sağlayıcı filtresiyle bulunabiliyor")
    void olusturVeFiltreyleBul() throws Exception {
        String gövde = """
                {"assetName": "rds-prod-01", "cloudProvider": "AWS", "region": "eu-central-1"}
                """;

        mockMvc.perform(post("/api/v1/cloud-assets").contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cloudProvider").value("AWS"));

        mockMvc.perform(get("/api/v1/cloud-assets").param("provider", "AWS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/api/v1/cloud-assets").param("provider", "GCP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("olmayan cloud varlığı id'si 404 döner")
    void olmayanKayit404() throws Exception {
        mockMvc.perform(get("/api/v1/cloud-assets/{id}", 987654))
                .andExpect(status().isNotFound());
    }
}
