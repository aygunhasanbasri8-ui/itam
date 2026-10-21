package com.itam.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.itam.AbstractIntegrationTest;

@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
@Transactional
@DisplayName("Asset silme (soft delete)")
class AssetDeletionControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("silinen asset detayda ve listede artık görünmüyor")
    void silinenAssetGorunmuyor() throws Exception {
        long id = sunucuOlustur("silinecek-01.example.local");

        mockMvc.perform(delete("/api/v1/assets/{id}", id)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/servers/{id}", id)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/servers").param("hostname", "silinecek-01"))
                .andExpect(jsonPath("$.totalElements").value(0));
        mockMvc.perform(delete("/api/v1/assets/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("bağımlılığı olan asset silinemiyor")
    void bagimliligiOlanSilinemez() throws Exception {
        long sunucu = sunucuOlustur("silinecek-02.example.local");
        String yanit = mockMvc.perform(post("/api/v1/applications").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"assetName": "Silme Testi Uygulaması"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long uygulama = objectMapper.readTree(yanit).get("id").asLong();

        mockMvc.perform(post("/api/v1/asset-dependencies").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parentAssetId": %d, "childAssetId": %d, "dependencyType": "RUNS_ON"}
                                """.formatted(uygulama, sunucu)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/assets/{id}", sunucu))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.code == 'bagimlilikVar')]").exists());
    }

    @Test
    @WithMockUser(username = "okuyucu", roles = "USER")
    @DisplayName("USER rolü silemez")
    void userRoluSilemez() throws Exception {
        mockMvc.perform(delete("/api/v1/assets/{id}", 1)).andExpect(status().isForbidden());
    }

    private long sunucuOlustur(String hostname) throws Exception {
        String yanit = mockMvc.perform(post("/api/v1/servers").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"assetName": "%s", "hostname": "%s", "environment": "TEST"}
                                """.formatted(hostname, hostname)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(yanit).get("id").asLong();
    }
}
