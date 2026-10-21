package com.itam.controller;

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
import com.itam.model.entity.Department;
import com.itam.repository.DepartmentRepository;

@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
@Transactional
@DisplayName("İş istasyonu CRUD")
class WorkstationAssetControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    DepartmentRepository departmentRepository;

    @Test
    @DisplayName("seri numarası boşsa 400 döner")
    void seriNumarasiBossaReddedilir() throws Exception {
        String gövde = """
                {"assetName": "masaüstü-01"}
                """;

        mockMvc.perform(post("/api/v1/workstations").contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'serialNumber')]").exists());
    }

    @Test
    @DisplayName("departman verilmeden kayıt açılabiliyor, sonra departman atanabiliyor")
    void departmansizAcilipSonraAtanabiliyor() throws Exception {
        String olusturmaGovdesi = """
                {"assetName": "masaüstü-02", "serialNumber": "SN-1001"}
                """;

        String yanit = mockMvc.perform(post("/api/v1/workstations")
                        .contentType(MediaType.APPLICATION_JSON).content(olusturmaGovdesi))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.department").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(yanit).get("id").asLong();

        Department departman = new Department();
        departman.setName("Bilgi Teknolojileri");
        departman.setCode("BT");
        departman = departmentRepository.save(departman);

        String guncellemeGovdesi = """
                {"assetName": "masaüstü-02", "serialNumber": "SN-1001", "departmentId": %d}
                """.formatted(departman.getId());

        mockMvc.perform(put("/api/v1/workstations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON).content(guncellemeGovdesi))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department.name").value("Bilgi Teknolojileri"));
    }

    @Test
    @DisplayName("var olmayan departman verilirse anlamlı hata döner")
    void gecersizDepartmanReddedilir() throws Exception {
        String gövde = """
                {"assetName": "masaüstü-03", "serialNumber": "SN-1002", "departmentId": 999999}
                """;

        mockMvc.perform(post("/api/v1/workstations").contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value(containsString("departman")));
    }

    @Test
    @DisplayName("olmayan iş istasyonu id'si 404 döner")
    void olmayanKayit404() throws Exception {
        mockMvc.perform(get("/api/v1/workstations/{id}", 987654))
                .andExpect(status().isNotFound());
    }
}
