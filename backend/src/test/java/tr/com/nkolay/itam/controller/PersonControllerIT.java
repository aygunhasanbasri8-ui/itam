package tr.com.nkolay.itam.controller;

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

import tr.com.nkolay.itam.AbstractIntegrationTest;
import tr.com.nkolay.itam.repository.PersonRepository;

@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
@Transactional
@DisplayName("Kişi CRUD")
class PersonControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PersonRepository personRepository;

    @Test
    @DisplayName("ad boşsa 400 döner")
    void adBossaReddedilir() throws Exception {
        String gövde = """
                {"active": true}
                """;

        mockMvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'fullName')]").exists());
    }

    @Test
    @DisplayName("oluşturulup güncellenebiliyor")
    void olusturVeGuncelle() throws Exception {
        String olusturmaGovdesi = """
                {"fullName": "Zeynep Kaya", "email": "zeynep.kaya@nkolay.com.tr", "active": true}
                """;

        String yanit = mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON).content(olusturmaGovdesi))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(yanit).get("id").asLong();

        String guncellemeGovdesi = """
                {"fullName": "Zeynep Kaya", "title": "Sistem Yöneticisi", "active": true}
                """;

        mockMvc.perform(put("/api/v1/people/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(guncellemeGovdesi))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sistem Yöneticisi"));
    }

    @Test
    @DisplayName("FIRMA sistem kaydı düzenlenemiyor")
    void firmaKaydiKorunuyor() throws Exception {
        long firmaId = personRepository.findByCode("FIRMA").orElseThrow().getId();

        String gövde = """
                {"fullName": "Değiştirilmiş İsim", "active": true}
                """;

        mockMvc.perform(put("/api/v1/people/{id}", firmaId)
                        .contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value(containsString("Sistem kaydı")));
    }

    @Test
    @DisplayName("var olmayan departman verilirse anlamlı hata döner")
    void gecersizDepartmanReddedilir() throws Exception {
        String gövde = """
                {"fullName": "Ahmet Demir", "active": true, "departmentId": 999999}
                """;

        mockMvc.perform(post("/api/v1/people").contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value(containsString("departman")));
    }

    @Test
    @DisplayName("olmayan kişi id'si 404 döner")
    void olmayanKayit404() throws Exception {
        mockMvc.perform(get("/api/v1/people/{id}", 987654))
                .andExpect(status().isNotFound());
    }
}
