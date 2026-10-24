package com.itam.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Lisans bitiş bildirimleri (itam.notifications.license-expiry.*). Hiç kanal
 * tanımlı değilse görev hiçbir şey yapmaz ve kayıt da düşmez; kanal sonradan
 * açılınca yaklaşan lisanslar için bildirim yine gider.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "itam.notifications.license-expiry")
public class LicenseNotificationProperties {

    private boolean enabled = true;
    /** Bitişten kaç gün önce haber verileceği. */
    private List<Integer> daysBefore = new ArrayList<>(List.of(30, 7, 1));
    /** JSON olarak POST edilir (Slack/Teams köprüsü, kendi servisiniz). */
    private String webhookUrl = "";
    /** Her bildirimin gideceği adresler; spring.mail.host da tanımlı olmalı. */
    private List<String> emailTo = new ArrayList<>();
    private String emailFrom = "itam@localhost";
    /** Lisansın sorumlusuna da e-posta gitsin mi (sistem kaydı FIRMA hariç). */
    private boolean notifyOwner = true;
}
