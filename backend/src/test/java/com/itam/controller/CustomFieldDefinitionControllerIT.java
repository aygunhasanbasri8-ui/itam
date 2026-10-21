package com.itam.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

/**
 * Faz 5 kabul testi: özel alan tanımı CRUD'u ve asset kaydına yazılan
 * custom_fields değerlerinin tanıma göre dinamik doğrulaması.
 */
@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
@Transactional
@DisplayName("Özel alan tanımları")
class CustomFieldDefinitionControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("tanım oluşturulup listeleniyor ve güncellenebiliyor")
    void tanimOlusturListeleGuncelle() throws Exception {
        long tanimId = tanimOlustur("SERVER", "rackNo", "Kabin No", "TEXT");

        mockMvc.perform(get("/api/v1/custom-field-definitions").param("assetTypeCode", "SERVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.fieldKey == 'rackNo')]").exists());

        String guncelleGovde = """
                {"label": "Kabin Numarası", "showInList": true, "active": true, "sortOrder": 5}
                """;
        mockMvc.perform(put("/api/v1/custom-field-definitions/{id}", tanimId)
                        .contentType(MediaType.APPLICATION_JSON).content(guncelleGovde))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Kabin Numarası"))
                .andExpect(jsonPath("$.sortOrder").value(5));
    }

    @Test
    @DisplayName("aynı asset tipinde aynı anahtar iki kere tanımlanamaz")
    void tekrarliAnahtarReddedilir() throws Exception {
        tanimOlustur("SERVER", "garantiTarihi", "Garanti Tarihi", "DATE");

        String govde = """
                {"assetTypeCode": "SERVER", "fieldKey": "garantiTarihi", "label": "Tekrar",
                 "dataType": "DATE", "showInList": false, "sortOrder": 0}
                """;
        mockMvc.perform(post("/api/v1/custom-field-definitions")
                        .contentType(MediaType.APPLICATION_JSON).content(govde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.code == 'tekil')]").exists());
    }

    @Test
    @DisplayName("tanımlı bir sayısal alana geçerli değer yazılınca asset kaydediliyor")
    void gecerliOzelAlanDegeriKabulEdiliyor() throws Exception {
        tanimOlustur("SERVER", "rackUnit", "Rack Unit", "NUMBER");

        String govde = """
                {"assetName": "cf-01", "hostname": "cf-01.example.local", "environment": "PROD",
                 "customFields": {"rackUnit": "12"}}
                """;
        mockMvc.perform(post("/api/v1/servers").contentType(MediaType.APPLICATION_JSON).content(govde))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customFields.rackUnit").value("12"));
    }

    @Test
    @DisplayName("sayısal alana metin değer yazılırsa reddedilir")
    void gecersizFormatReddedilir() throws Exception {
        tanimOlustur("SERVER", "rackUnit2", "Rack Unit 2", "NUMBER");

        String govde = """
                {"assetName": "cf-02", "hostname": "cf-02.example.local", "environment": "PROD",
                 "customFields": {"rackUnit2": "oniki"}}
                """;
        mockMvc.perform(post("/api/v1/servers").contentType(MediaType.APPLICATION_JSON).content(govde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.code == 'gecersizOzelAlanDegeri')]").exists());
    }

    @Test
    @DisplayName("tanımsız özel alan anahtarı reddedilir")
    void tanimsizAlanReddedilir() throws Exception {
        String govde = """
                {"assetName": "cf-03", "hostname": "cf-03.example.local", "environment": "PROD",
                 "customFields": {"hicTanimliDegil": "x"}}
                """;
        mockMvc.perform(post("/api/v1/servers").contentType(MediaType.APPLICATION_JSON).content(govde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.code == 'tanimsizOzelAlan')]").exists());
    }

    @Test
    @DisplayName("pasife alınan alanın eski değeri asset'i kaydetmeye engel olmaz")
    void pasifAlanDegeriKabulEdiliyor() throws Exception {
        long tanimId = tanimOlustur("SERVER", "eskiEtiket", "Eski Etiket", "TEXT");
        mockMvc.perform(put("/api/v1/custom-field-definitions/{id}", tanimId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"label": "Eski Etiket", "showInList": false, "active": false, "sortOrder": 0}
                                """))
                .andExpect(status().isOk());

        String govde = """
                {"assetName": "cf-04", "hostname": "cf-04.example.local", "environment": "PROD",
                 "customFields": {"eskiEtiket": "A-12"}}
                """;
        mockMvc.perform(post("/api/v1/servers").contentType(MediaType.APPLICATION_JSON).content(govde))
                .andExpect(status().isCreated());
    }

    private long tanimOlustur(String assetTypeCode, String fieldKey, String label, String dataType) throws Exception {
        String govde = """
                {"assetTypeCode": "%s", "fieldKey": "%s", "label": "%s",
                 "dataType": "%s", "showInList": false, "sortOrder": 0}
                """.formatted(assetTypeCode, fieldKey, label, dataType);
        String yanit = mockMvc.perform(post("/api/v1/custom-field-definitions")
                        .contentType(MediaType.APPLICATION_JSON).content(govde))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(yanit).get("id").asLong();
    }
}
