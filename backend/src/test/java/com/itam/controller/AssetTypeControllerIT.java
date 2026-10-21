package com.itam.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.itam.AbstractIntegrationTest;

@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
@Transactional
@DisplayName("Asset tipleri")
class AssetTypeControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("tipler sıralı ve Türkçe adlarıyla listeleniyor, görünen bilgiler güncellenebiliyor")
    void listeleVeGuncelle() throws Exception {
        mockMvc.perform(get("/api/v1/asset-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SERVER"))
                .andExpect(jsonPath("$[?(@.code == 'WORKSTATION')].displayName").value("İş İstasyonu"));

        mockMvc.perform(put("/api/v1/asset-types/{code}", "CLOUD").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName": "Bulut Varlığı", "icon": "fa-cloud", "sortOrder": 35}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Bulut Varlığı"))
                .andExpect(jsonPath("$.sortOrder").value(35));
    }

    @Test
    @DisplayName("tanımsız tip kodu 404 döner")
    void tanimsizTip404() throws Exception {
        mockMvc.perform(put("/api/v1/asset-types/{code}", "YOK").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName": "x", "icon": "fa-cube", "sortOrder": 0}
                                """))
                .andExpect(status().isNotFound());
    }
}
