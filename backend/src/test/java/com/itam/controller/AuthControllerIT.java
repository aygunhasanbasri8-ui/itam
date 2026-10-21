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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.itam.AbstractIntegrationTest;

/**
 * Diğer *ControllerIT'ler @WithMockUser ile Spring Security'yi atlıyor; bu
 * sınıf gerçek zinciri (login -> JWT üret -> Bearer başlığıyla korumalı uca
 * git) en az bir kez uçtan uca doğrular.
 */
@AutoConfigureMockMvc
@Transactional
@DisplayName("Giriş ve JWT doğrulama")
class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("doğru bilgiyle giriş yapıp dönen token'la korumalı uca erişilebiliyor")
    void girisYapVeTokenIleErisebiliyor() throws Exception {
        String gövde = """
                {"username": "admin", "password": "admin123"}
                """;

        String yanit = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(yanit).get("token").asText();

        mockMvc.perform(get("/api/v1/servers").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("yanlış şifreyle 401 döner")
    void yanlisSifreReddedilir() throws Exception {
        String gövde = """
                {"username": "admin", "password": "yanlis-sifre"}
                """;

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("token olmadan korumalı uca erişilemiyor")
    void tokensizErisilemiyor() throws Exception {
        mockMvc.perform(get("/api/v1/servers")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("token olmadan yazma uç noktasına da erişilemiyor")
    void tokensizYazmaEndpointi401() throws Exception {
        mockMvc.perform(post("/api/v1/servers")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("pasife alınan kullanıcının elindeki token artık geçmiyor")
    void pasifKullaniciTokeniGecmiyor() throws Exception {
        String adminToken = girisYap("admin", "admin123");

        String yanit = mockMvc.perform(post("/api/v1/users").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "gecici", "password": "gecici-sifre-1", "role": "USER"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long kullaniciId = objectMapper.readTree(yanit).get("id").asLong();

        String kullaniciToken = girisYap("gecici", "gecici-sifre-1");
        mockMvc.perform(get("/api/v1/servers").header("Authorization", "Bearer " + kullaniciToken))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/users/{id}", kullaniciId).header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "USER", "active": false}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/servers").header("Authorization", "Bearer " + kullaniciToken))
                .andExpect(status().isUnauthorized());
    }

    private String girisYap(String kullaniciAdi, String sifre) throws Exception {
        String yanit = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "%s", "password": "%s"}
                                """.formatted(kullaniciAdi, sifre)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(yanit).get("token").asText();
    }
}
