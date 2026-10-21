package com.itam.controller;

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
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.itam.AbstractIntegrationTest;

/**
 * Faz 5 kabul testi: Envers'ın tuttuğu revizyonların alan bazlı değişiklik
 * geçmişine doğru çevrildiğini doğrular.
 */
@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
@Transactional
@DisplayName("Denetim izi")
class AssetAuditControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("oluşturma ve durum değişikliği iki ayrı revizyon olarak görünüyor")
    void olusturmaVeDurumDegisikligiRevizyonlaniyor() throws Exception {
        String olusturmaGovdesi = """
                {"assetName": "audit-01", "hostname": "audit-01.example.local", "environment": "PROD"}
                """;

        String yanit = mockMvc.perform(post("/api/v1/servers")
                        .contentType(MediaType.APPLICATION_JSON).content(olusturmaGovdesi))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(yanit).get("id").asLong();

        // Envers, revizyon satırlarını flush'ta değil transaction commit olmadan hemen
        // önce yazıyor; testin @Transactional sarmalayıcısı hiç commit etmediği için
        // iki yazımı da gerçek commit ile ayırmamız gerekiyor, yoksa audit trail boş döner.
        commit();

        String bulkGovde = """
                {"assetIds": [%d], "status": "MAINTENANCE"}
                """.formatted(id);
        mockMvc.perform(patch("/api/v1/assets/bulk-status")
                        .contentType(MediaType.APPLICATION_JSON).content(bulkGovde))
                .andExpect(status().isOk());

        commit();

        mockMvc.perform(get("/api/v1/assets/{id}/audit-trail", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].revisionType").value("ADD"))
                .andExpect(jsonPath("$[0].changes[?(@.field == 'status')].newValue").value("ACTIVE"))
                .andExpect(jsonPath("$[1].revisionType").value("MOD"))
                .andExpect(jsonPath("$[1].changes[?(@.field == 'status')].oldValue").value("ACTIVE"))
                .andExpect(jsonPath("$[1].changes[?(@.field == 'status')].newValue").value("MAINTENANCE"));
    }

    @Test
    @DisplayName("olmayan asset için 404 döner")
    void olmayanAssetIcin404() throws Exception {
        mockMvc.perform(get("/api/v1/assets/{id}/audit-trail", 987654))
                .andExpect(status().isNotFound());
    }

    private void commit() {
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }
}
