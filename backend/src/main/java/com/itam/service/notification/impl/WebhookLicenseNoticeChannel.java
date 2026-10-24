package com.itam.service.notification.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itam.config.LicenseNotificationProperties;
import com.itam.service.notification.LicenseNotice;
import com.itam.service.notification.LicenseNoticeChannel;

@Component
public class WebhookLicenseNoticeChannel implements LicenseNoticeChannel {

    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private final LicenseNotificationProperties properties;
    private final ObjectMapper mapper;

    public WebhookLicenseNoticeChannel(LicenseNotificationProperties properties, ObjectMapper mapper) {
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    public String name() {
        return "webhook";
    }

    @Override
    public boolean isConfigured() {
        return !properties.getWebhookUrl().isBlank();
    }

    @Override
    public void send(LicenseNotice notice) throws IOException, InterruptedException {
        Map<String, Object> body = new LinkedHashMap<>();
        // "text" alanı Slack/Teams gelen webhook'larının doğrudan gösterdiği alan
        body.put("text", notice.text());
        body.put("assetId", notice.assetId());
        body.put("assetName", notice.assetName());
        body.put("expiryDate", notice.expiryDate().toString());
        body.put("daysLeft", notice.daysLeft());
        HttpRequest request = HttpRequest.newBuilder(URI.create(properties.getWebhookUrl()))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();
        HttpResponse<Void> response = CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        if (response.statusCode() >= 300) {
            throw new IOException("HTTP " + response.statusCode());
        }
    }
}
