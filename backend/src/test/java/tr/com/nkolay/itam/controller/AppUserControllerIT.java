package tr.com.nkolay.itam.controller;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tr.com.nkolay.itam.AbstractIntegrationTest;

@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
@Transactional
@DisplayName("Kullanıcı yönetimi")
class AppUserControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("oluşturulan kullanıcı kendi şifresiyle giriş yapabiliyor")
    void olusturulanKullaniciGirisYapabiliyor() throws Exception {
        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content("""
                        {"username": "ayse", "password": "guclu-sifre-1", "role": "USER"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"username": "ayse", "password": "guclu-sifre-1"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("aynı kullanıcı adı ikinci kez alınamaz")
    void tekrarliKullaniciAdiReddedilir() throws Exception {
        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content("""
                        {"username": "admin", "password": "baska-sifre-1", "role": "USER"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.code == 'tekil')]").exists());
    }

    @Test
    @DisplayName("son aktif yönetici USER'a düşürülemez")
    void sonAdminDusurulemez() throws Exception {
        String liste = mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long adminId = 0;
        for (JsonNode user : objectMapper.readTree(liste)) {
            if (user.get("username").asText().equals("admin")) {
                adminId = user.get("id").asLong();
            }
        }

        mockMvc.perform(put("/api/v1/users/{id}", adminId).contentType(MediaType.APPLICATION_JSON).content("""
                        {"role": "USER", "active": true}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.code == 'sonAdmin')]").exists());
    }

    @Test
    @WithMockUser(username = "okuyucu", roles = "USER")
    @DisplayName("USER rolü kullanıcı listesini göremez")
    void userRoluListeyiGoremez() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }
}
