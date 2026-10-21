package com.itam.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
@DisplayName("Toplu durum güncelleme")
class AssetBulkControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("iki sunucu tek istekte MAINTENANCE'a alınabiliyor")
    void ikiSunucuTopluGuncellenebiliyor() throws Exception {
        long id1 = sunucuOlustur("bulk-01.example.local");
        long id2 = sunucuOlustur("bulk-02.example.local");

        String gövde = """
                {"assetIds": [%d, %d], "status": "MAINTENANCE"}
                """.formatted(id1, id2);

        mockMvc.perform(patch("/api/v1/assets/bulk-status")
                        .contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedCount").value(2));

        mockMvc.perform(get("/api/v1/servers/{id}", id1))
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));
        mockMvc.perform(get("/api/v1/servers/{id}", id2))
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));
    }

    @Test
    @DisplayName("listede olmayan bir id varsa hiçbir kayıt güncellenmiyor")
    void gecersizIdVarsaHicbiriGuncellenmiyor() throws Exception {
        long id1 = sunucuOlustur("bulk-03.example.local");

        String gövde = """
                {"assetIds": [%d, 987654], "status": "RETIRED"}
                """.formatted(id1);

        mockMvc.perform(patch("/api/v1/assets/bulk-status")
                        .contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value(containsString("bulunamadı")));

        // Geçerli olan da güncellenmemiş olmalı - hepsi ya da hiçbiri.
        mockMvc.perform(get("/api/v1/servers/{id}", id1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    private long sunucuOlustur(String hostname) throws Exception {
        String gövde = """
                {"assetName": "%s", "hostname": "%s", "environment": "PROD"}
                """.formatted(hostname, hostname);
        String yanit = mockMvc.perform(post("/api/v1/servers")
                        .contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(yanit).get("id").asLong();
    }
}
