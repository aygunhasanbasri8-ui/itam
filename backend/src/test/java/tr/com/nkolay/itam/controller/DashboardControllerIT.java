package tr.com.nkolay.itam.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.AbstractIntegrationTest;

/**
 * Faz 6 kabul testi: dashboard özet sayaçlarının asset/lisans kayıtlarını
 * doğru şekilde topladığını doğrular.
 */
@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
@Transactional
@DisplayName("Dashboard özeti")
class DashboardControllerIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("sunucu ve süresi yaklaşan lisans sayaçları doğru geliyor")
    void ozetSayaclarDogruGeliyor() throws Exception {
        String sunucuGovde = """
                {"assetName": "dash-01", "hostname": "dash-01.nkolay.local", "environment": "PROD"}
                """;
        mockMvc.perform(post("/api/v1/servers").contentType(MediaType.APPLICATION_JSON).content(sunucuGovde))
                .andExpect(status().isCreated());

        String yakinTarih = LocalDate.now().plusDays(5).toString();
        String uygulamaGovde = """
                {"assetName": "dash-app", "expiryDate": "%s"}
                """.formatted(yakinTarih);
        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON).content(uygulamaGovde))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/dashboard/summary").param("expiringSoonDays", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countByAssetType.SERVER").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.countByEnvironment.PROD").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.licensesExpiringSoon").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.recentAssets.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }
}
