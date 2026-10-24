package com.itam.service.notification.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.itam.config.LicenseNotificationProperties;
import com.itam.service.notification.LicenseNotice;
import com.itam.service.notification.LicenseNoticeChannel;

/** spring.mail.host tanımlı değilse JavaMailSender oluşmuyor, kanal kapalı kalıyor. */
@Component
public class EmailLicenseNoticeChannel implements LicenseNoticeChannel {

    private final LicenseNotificationProperties properties;
    private final ObjectProvider<JavaMailSender> mailSender;

    public EmailLicenseNoticeChannel(LicenseNotificationProperties properties,
                                     ObjectProvider<JavaMailSender> mailSender) {
        this.properties = properties;
        this.mailSender = mailSender;
    }

    @Override
    public String name() {
        return "email";
    }

    @Override
    public boolean isConfigured() {
        return mailSender.getIfAvailable() != null
                && (!properties.getEmailTo().isEmpty() || properties.isNotifyOwner());
    }

    @Override
    public void send(LicenseNotice notice) {
        Set<String> to = new LinkedHashSet<>(properties.getEmailTo());
        if (properties.isNotifyOwner() && notice.ownerEmail() != null && !notice.ownerEmail().isBlank()) {
            to.add(notice.ownerEmail());
        }
        if (to.isEmpty()) {
            return;
        }
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(properties.getEmailFrom());
        mail.setTo(to.toArray(String[]::new));
        mail.setSubject("[ITAM] " + notice.subject());
        mail.setText(notice.text());
        mailSender.getObject().send(mail);
    }
}
