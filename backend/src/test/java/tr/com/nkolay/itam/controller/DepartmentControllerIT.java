package tr.com.nkolay.itam.controller;

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

@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
@Transactional
@DisplayName("Departman CRUD")
class DepartmentControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("kod boşsa 400 döner")
    void kodBossaReddedilir() throws Exception {
        String gövde = """
                {"name": "Bilgi Teknolojileri", "active": true}
                """;

        mockMvc.perform(post("/api/v1/departments").contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'code')]").exists());
    }

    @Test
    @DisplayName("oluşturulup pasife alınabiliyor")
    void olusturVePasifeAl() throws Exception {
        String olusturmaGovdesi = """
                {"name": "İnsan Kaynakları", "code": "IK", "active": true}
                """;

        String yanit = mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON).content(olusturmaGovdesi))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(yanit).get("id").asLong();

        String guncellemeGovdesi = """
                {"name": "İnsan Kaynakları", "code": "IK", "active": false}
                """;

        mockMvc.perform(put("/api/v1/departments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(guncellemeGovdesi))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }
}
