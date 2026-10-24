package com.itam.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itam.AbstractIntegrationTest;
import com.itam.service.notification.LicenseExpiryNotificationService;
import com.itam.service.notification.LicenseNotice;
import com.itam.service.notification.LicenseNoticeChannel;

@AutoConfigureMockMvc
@WithMockUser(username = "test-admin", roles = "ADMIN")
@Transactional
@Import(LicenseExpiryNotificationIT.Capture.class)
@DisplayName("Lisans bitiş bildirimleri")
class LicenseExpiryNotificationIT extends AbstractIntegrationTest {

    // Gerçek tarihlerden bağımsız olsun diye uzak bir gelecek
    private static final LocalDate TODAY = LocalDate.of(2031, 3, 1);

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    LicenseExpiryNotificationService service;

    @Autowired
    Capture capture;

    @BeforeEach
    void clear() {
        capture.sent.clear();
    }

    @Test
    @DisplayName("her eşik bir kez gönderiliyor, aynı gün ikinci tur sessiz")
    void herEsikBirKez() throws Exception {
        long id = uygulama("Lisans-30", TODAY.plusDays(30));

        assertThat(service.runOnce(TODAY)).isEqualTo(1);
        assertThat(service.runOnce(TODAY)).isZero();
        assertThat(service.runOnce(TODAY.plusDays(10))).isZero();   // 20 gün kala, yeni eşik yok
        assertThat(service.runOnce(TODAY.plusDays(23))).isEqualTo(1);  // 7 gün kala
        assertThat(service.runOnce(TODAY.plusDays(29))).isEqualTo(1);  // 1 gün kala

        assertThat(capture.sent).extracting(LicenseNotice::daysLeft).containsExactly(30L, 7L, 1L);
        assertThat(capture.sent).allMatch(n -> n.assetId() == id);
    }

    @Test
    @DisplayName("geç eklenen lisans geçilmiş büyük eşikler için ayrıca bildirim almıyor")
    void gecEklenenTekBildirim() throws Exception {
        uygulama("Lisans-5", TODAY.plusDays(5));

        assertThat(service.runOnce(TODAY)).isEqualTo(1);
        assertThat(capture.sent.get(0).daysLeft()).isEqualTo(5);
        assertThat(service.runOnce(TODAY.plusDays(1))).isZero();
        assertThat(service.runOnce(TODAY.plusDays(4))).isEqualTo(1);
    }

    @Test
    @DisplayName("bitiş tarihi uzatılınca bildirimler yeni tarih için yeniden başlıyor")
    void tarihUzatilinca() throws Exception {
        long id = uygulama("Lisans-uzayan", TODAY.plusDays(7));
        service.runOnce(TODAY);

        mockMvc.perform(put("/api/v1/applications/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                {"assetName": "Lisans-uzayan", "expiryDate": "%s"}
                """.formatted(TODAY.plusDays(30)))).andExpect(status().isOk());

        assertThat(service.runOnce(TODAY)).isEqualTo(1);
        assertThat(capture.sent).extracting(LicenseNotice::daysLeft).containsExactly(7L, 30L);
    }

    private long uygulama(String name, LocalDate expiry) throws Exception {
        String yanit = mockMvc.perform(post("/api/v1/applications").contentType(MediaType.APPLICATION_JSON).content("""
                        {"assetName": "%s", "expiryDate": "%s", "vendor": "Örnek AŞ"}
                        """.formatted(name, expiry)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(yanit).get("id").asLong();
    }

    @TestConfiguration
    static class Capture {
        final List<LicenseNotice> sent = new ArrayList<>();

        @Bean
        LicenseNoticeChannel captureChannel() {
            return new LicenseNoticeChannel() {
                public String name() { return "test"; }
                public boolean isConfigured() { return true; }
                public void send(LicenseNotice notice) { sent.add(notice); }
            };
        }
    }
}
