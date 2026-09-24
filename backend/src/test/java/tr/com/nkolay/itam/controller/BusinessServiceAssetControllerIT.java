package tr.com.nkolay.itam.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.AbstractIntegrationTest;

@AutoConfigureMockMvc
@Transactional
@DisplayName("İş servisi CRUD")
class BusinessServiceAssetControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("kritiklik belirtilmezse MID atanır")
    void kritiklikBelirtilmezseMidAtanir() throws Exception {
        String gövde = """
                {"assetName": "Ödeme API'si"}
                """;

        mockMvc.perform(post("/api/v1/business-services").contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.criticality").value("MID"));
    }

    @Test
    @DisplayName("kritiklik filtresiyle liste daralıyor")
    void kritiklikFiltresiCalisiyor() throws Exception {
        olustur("3D Secure Servisi", "HIGH");
        olustur("Raporlama Servisi", "LOW");

        mockMvc.perform(get("/api/v1/business-services").param("criticality", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].assetName").value("3D Secure Servisi"));
    }

    @Test
    @DisplayName("olmayan iş servisi id'si 404 döner")
    void olmayanKayit404() throws Exception {
        mockMvc.perform(get("/api/v1/business-services/{id}", 987654))
                .andExpect(status().isNotFound());
    }

    private void olustur(String assetName, String criticality) throws Exception {
        String gövde = """
                {"assetName": "%s", "criticality": "%s"}
                """.formatted(assetName, criticality);
        mockMvc.perform(post("/api/v1/business-services")
                        .contentType(MediaType.APPLICATION_JSON).content(gövde))
                .andExpect(status().isCreated());
    }
}
