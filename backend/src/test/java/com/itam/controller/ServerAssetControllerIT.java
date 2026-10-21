package com.itam.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.itam.AbstractIntegrationTest;
import com.itam.enums.ServerEnvironment;
import com.itam.model.entity.Person;
import com.itam.repository.PersonRepository;

/**
 * Faz 1 kabul testi: sunucu ekle/görüntüle/güncelle/listele uçtan uca.
 * Her metot @Transactional, testler arası veri birbirine karışmasın diye
 * (rollback sayesinde ayrı ayrı boş DB'den başlıyorlar).
 */
@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
@Transactional
@DisplayName("Sunucu CRUD")
class ServerAssetControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PersonRepository personRepository;

    @Test
    @DisplayName("sorumlu belirtilmeden kayıt açılırsa FIRMA'ya atanır")
    void sorumluBelirtilmezseFirmayaAtanir() throws Exception {
        String gövde = """
                {
                  "assetName": "app-db-01",
                  "hostname": "app-db-01.example.local",
                  "environment": "PROD"
                }
                """;

        mockMvc.perform(post("/api/v1/servers").contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hostname").value("app-db-01.example.local"))
                .andExpect(jsonPath("$.responsibilityOwner.fullName").value("Firma"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("hostname boşsa 400 döner")
    void hostnameBossaReddedilir() throws Exception {
        String gövde = """
                {
                  "assetName": "isimsiz-sunucu",
                  "environment": "DEV"
                }
                """;

        mockMvc.perform(post("/api/v1/servers").contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'hostname')]").exists());
    }

    @Test
    @DisplayName("var olmayan sorumlu kişi verilirse anlamlı hata döner")
    void gecersizSorumluReddedilir() throws Exception {
        String gövde = """
                {
                  "assetName": "app-cache-01",
                  "hostname": "app-cache-01.example.local",
                  "environment": "TEST",
                  "responsibilityOwnerId": 999999
                }
                """;

        mockMvc.perform(post("/api/v1/servers").contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value(containsString("sorumlu")));
    }

    @Test
    @DisplayName("kayıt oluşturulup sonra güncellenebiliyor")
    void olusturVeGuncelle() throws Exception {
        Person sorumlu = personRepository.save(yeniKisi("Ayşe Yılmaz"));

        String olusturmaGovdesi = """
                {
                  "assetName": "web-01",
                  "hostname": "web-01.example.local",
                  "environment": "UAT",
                  "responsibilityOwnerId": %d
                }
                """.formatted(sorumlu.getId());

        String yanit = mockMvc.perform(post("/api/v1/servers")
                        .contentType(MediaType.APPLICATION_JSON).content(olusturmaGovdesi))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(yanit).get("id").asLong();

        String guncellemeGovdesi = """
                {
                  "assetName": "web-01",
                  "hostname": "web-01.example.local",
                  "environment": "PROD",
                  "responsibilityOwnerId": %d,
                  "os": "Ubuntu 24.04"
                }
                """.formatted(sorumlu.getId());

        mockMvc.perform(put("/api/v1/servers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(guncellemeGovdesi))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.environment").value("PROD"))
                .andExpect(jsonPath("$.os").value("Ubuntu 24.04"));
    }

    @Test
    @DisplayName("olmayan sunucu id'si 404 döner")
    void olmayanSunucu404() throws Exception {
        mockMvc.perform(get("/api/v1/servers/{id}", 987654))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ortam filtresiyle liste daralıyor")
    void ortamFiltresiCalisiyor() throws Exception {
        olustur("prod-01.example.local", ServerEnvironment.PROD);
        olustur("dev-01.example.local", ServerEnvironment.DEV);

        mockMvc.perform(get("/api/v1/servers").param("environment", "PROD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].hostname",
                        org.hamcrest.Matchers.everyItem(containsString("prod"))));
    }

    private void olustur(String hostname, ServerEnvironment environment) throws Exception {
        String gövde = """
                {"assetName": "%s", "hostname": "%s", "environment": "%s"}
                """.formatted(hostname, hostname, environment);
        mockMvc.perform(post("/api/v1/servers").contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isCreated());
    }

    private Person yeniKisi(String adSoyad) {
        Person p = new Person();
        p.setFullName(adSoyad);
        p.setActive(true);
        return p;
    }
}
