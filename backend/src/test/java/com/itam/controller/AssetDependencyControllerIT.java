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

/**
 * Faz 5 kabul testi: iş servisi -> uygulama -> sunucu bağımlılık zinciri
 * kurulup ağaç ve döngü kontrolleri uçtan uca doğrulanıyor.
 */
@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
@Transactional
@DisplayName("Bağımlılık haritası")
class AssetDependencyControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("iş servisi -> uygulama -> sunucu zinciri ağaç olarak dönüyor")
    void bagimlilikAgaciKuruluyor() throws Exception {
        long sunucu = sunucuOlustur("db-01.example.local");
        long uygulama = uygulamaOlustur("Muhasebe Uygulaması");
        long servis = servisOlustur("Muhasebe Servisi");

        bagimlilikEkle(servis, uygulama, "USES");
        bagimlilikEkle(uygulama, sunucu, "RUNS_ON");

        mockMvc.perform(get("/api/v1/assets/{id}/dependency-tree", servis))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetId").value(servis))
                .andExpect(jsonPath("$.children[0].assetId").value(uygulama))
                .andExpect(jsonPath("$.children[0].dependencyType").value("USES"))
                .andExpect(jsonPath("$.children[0].children[0].assetId").value(sunucu))
                .andExpect(jsonPath("$.children[0].children[0].dependencyType").value("RUNS_ON"));

        mockMvc.perform(get("/api/v1/assets/{id}/dependencies", sunucu))
                .andExpect(jsonPath("$[0].createdBy").value("test-admin"));
    }

    @Test
    @DisplayName("bir asset kendisine bağımlı olamaz")
    void kendineBagimlilikReddedilir() throws Exception {
        long sunucu = sunucuOlustur("self-01.example.local");

        String gövde = """
                {"parentAssetId": %d, "childAssetId": %d, "dependencyType": "RUNS_ON"}
                """.formatted(sunucu, sunucu);

        mockMvc.perform(post("/api/v1/asset-dependencies")
                        .contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.code == 'kendineBagimlilik')]").exists());
    }

    @Test
    @DisplayName("döngü oluşturacak bağımlılık reddedilir")
    void donguReddedilir() throws Exception {
        long uygulama = uygulamaOlustur("Döngü Uygulaması");
        long sunucu = sunucuOlustur("dongu-01.example.local");

        bagimlilikEkle(uygulama, sunucu, "RUNS_ON");

        String gövde = """
                {"parentAssetId": %d, "childAssetId": %d, "dependencyType": "DEPENDS_ON"}
                """.formatted(sunucu, uygulama);

        mockMvc.perform(post("/api/v1/asset-dependencies")
                        .contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.code == 'donguselBagimlilik')]").exists());
    }

    @Test
    @DisplayName("aynı bağımlılık iki kere eklenemez")
    void tekrarliBagimlilikReddedilir() throws Exception {
        long uygulama = uygulamaOlustur("Tekrar Uygulaması");
        long sunucu = sunucuOlustur("tekrar-01.example.local");

        bagimlilikEkle(uygulama, sunucu, "RUNS_ON");

        String gövde = """
                {"parentAssetId": %d, "childAssetId": %d, "dependencyType": "RUNS_ON"}
                """.formatted(uygulama, sunucu);

        mockMvc.perform(post("/api/v1/asset-dependencies")
                        .contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.code == 'bagimlilikZatenVar')]").exists());
    }

    @Test
    @DisplayName("bağımlılık silinebiliyor")
    void bagimlilikSilinebiliyor() throws Exception {
        long uygulama = uygulamaOlustur("Silinecek Uygulama");
        long sunucu = sunucuOlustur("silinecek-01.example.local");

        long dependencyId = bagimlilikEkle(uygulama, sunucu, "RUNS_ON");

        mockMvc.perform(delete("/api/v1/asset-dependencies/{id}", dependencyId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/assets/{id}/dependencies", uygulama))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    private long sunucuOlustur(String hostname) throws Exception {
        String gövde = """
                {"assetName": "%s", "hostname": "%s", "environment": "PROD"}
                """.formatted(hostname, hostname);
        return olusturVeIdDondur("/api/v1/servers", gövde);
    }

    private long uygulamaOlustur(String isim) throws Exception {
        String gövde = """
                {"assetName": "%s"}
                """.formatted(isim);
        return olusturVeIdDondur("/api/v1/applications", gövde);
    }

    private long servisOlustur(String isim) throws Exception {
        String gövde = """
                {"assetName": "%s"}
                """.formatted(isim);
        return olusturVeIdDondur("/api/v1/business-services", gövde);
    }

    private long olusturVeIdDondur(String yol, String gövde) throws Exception {
        String yanit = mockMvc.perform(post(yol).contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(yanit).get("id").asLong();
    }

    private long bagimlilikEkle(long parentId, long childId, String tip) throws Exception {
        String gövde = """
                {"parentAssetId": %d, "childAssetId": %d, "dependencyType": "%s"}
                """.formatted(parentId, childId, tip);
        String yanit = mockMvc.perform(post("/api/v1/asset-dependencies")
                        .contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(yanit).get("id").asLong();
    }
}
