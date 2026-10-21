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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.AbstractIntegrationTest;

@AutoConfigureMockMvc
@WithMockUser(username = "okuyucu", roles = "USER")
@Transactional
@DisplayName("Tipten bağımsız asset arama")
class AssetSearchControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(username = "test-admin", roles = "ADMIN")
    @DisplayName("farklı tipteki asset'ler isimle tek aramada bulunuyor")
    void farkliTiplerTekAramadaBulunuyor() throws Exception {
        mockMvc.perform(post("/api/v1/servers").contentType(MediaType.APPLICATION_JSON).content("""
                        {"assetName": "Muhasebe DB", "hostname": "arama-01.nkolay.local", "environment": "PROD"}
                        """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/applications").contentType(MediaType.APPLICATION_JSON).content("""
                        {"assetName": "Muhasebe Uygulaması"}
                        """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/assets/search").param("q", "muhasebe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].assetName").value("Muhasebe DB"))
                .andExpect(jsonPath("$[0].assetTypeCode").value("SERVER"))
                .andExpect(jsonPath("$[1].assetTypeCode").value("APPLICATION"));
    }

    @Test
    @DisplayName("USER rolü de arama yapabiliyor")
    void userRoluAramaYapabiliyor() throws Exception {
        mockMvc.perform(get("/api/v1/assets/search").param("q", "yok-boyle-bir-sey"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
