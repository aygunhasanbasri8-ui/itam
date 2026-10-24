package com.itam.service.notification.impl;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.itam.config.LicenseNotificationProperties;
import com.itam.model.entity.ApplicationAsset;
import com.itam.model.entity.LicenseExpiryNotice;
import com.itam.model.entity.Person;
import com.itam.repository.ApplicationAssetRepository;
import com.itam.repository.LicenseExpiryNoticeRepository;
import com.itam.service.notification.LicenseExpiryNotificationService;
import com.itam.service.notification.LicenseNotice;
import com.itam.service.notification.LicenseNoticeChannel;

@Service
public class LicenseExpiryNotificationServiceImpl implements LicenseExpiryNotificationService {

    private static final Logger log = LoggerFactory.getLogger(LicenseExpiryNotificationServiceImpl.class);

    private final LicenseNotificationProperties properties;
    private final ApplicationAssetRepository applicationRepository;
    private final LicenseExpiryNoticeRepository noticeRepository;
    private final List<LicenseNoticeChannel> channels;

    public LicenseExpiryNotificationServiceImpl(LicenseNotificationProperties properties,
                                                ApplicationAssetRepository applicationRepository,
                                                LicenseExpiryNoticeRepository noticeRepository,
                                                List<LicenseNoticeChannel> channels) {
        this.properties = properties;
        this.applicationRepository = applicationRepository;
        this.noticeRepository = noticeRepository;
        this.channels = channels;
    }

    /** Saatte bir: kapalı kalınan bir sabahın bildirimi uygulama açılınca yine gider. */
    @Scheduled(cron = "${itam.notifications.license-expiry.cron:0 7 * * * *}")
    public void scheduledRun() {
        runOnce(LocalDate.now());
    }

    // Bilerek transaction'sız: kayıtlar tek tek yazılıyor, bir çakışma turun geri
    // kalanını geri aldırmasın. Sorumlu EntityGraph ile birlikte geldiği için lazy yükleme yok.
    @Override
    public int runOnce(LocalDate today) {
        List<LicenseNoticeChannel> active = channels.stream().filter(LicenseNoticeChannel::isConfigured).toList();
        TreeSet<Integer> thresholds = new TreeSet<>(properties.getDaysBefore());
        thresholds.removeIf(d -> d == null || d < 0);
        if (!properties.isEnabled() || active.isEmpty() || thresholds.isEmpty()) {
            return 0;
        }

        int sent = 0;
        List<ApplicationAsset> soon = applicationRepository
                .findByDeletedFalseAndExpiryDateBetween(today, today.plusDays(thresholds.last()));
        for (ApplicationAsset app : soon) {
            long daysLeft = ChronoUnit.DAYS.between(today, app.getExpiryDate());
            // Geçilmiş eşiklerin en küçüğü için tek bildirim. 5 gün kala eklenen bir lisans
            // için önce "30 gün", hemen ardından "7 gün" diye iki mesaj gitmesin; geçilmiş
            // büyük eşikler de gönderilmiş sayılıyor.
            Set<Integer> crossed = new HashSet<>(thresholds.tailSet((int) daysLeft, true));
            if (crossed.isEmpty()) {
                continue;
            }
            Set<Integer> done = new HashSet<>();
            noticeRepository.findByAssetIdAndExpiryDate(app.getId(), app.getExpiryDate())
                    .forEach(n -> done.add(n.getDaysBefore()));
            int current = thresholds.ceiling((int) daysLeft);
            if (done.contains(current)) {
                continue;
            }

            LicenseNotice notice = toNotice(app, daysLeft);
            boolean delivered = false;
            for (LicenseNoticeChannel channel : active) {
                try {
                    channel.send(notice);
                    delivered = true;
                } catch (Exception e) {
                    log.warn("Lisans bildirimi gönderilemedi ({}, {}): {}", channel.name(), app.getId(), e.getMessage());
                }
            }
            // Hiçbir kanal ulaştıramadıysa kaydetmiyoruz, bir sonraki turda yeniden denenir
            if (!delivered) {
                continue;
            }
            for (int d : crossed) {
                if (!done.contains(d)) {
                    saveIgnoringDuplicate(new LicenseExpiryNotice(app.getId(), app.getExpiryDate(), d,
                            OffsetDateTime.now()));
                }
            }
            sent++;
        }
        if (sent > 0) {
            log.info("{} lisans bitiş bildirimi gönderildi", sent);
        }
        return sent;
    }

    private void saveIgnoringDuplicate(LicenseExpiryNotice notice) {
        try {
            noticeRepository.saveAndFlush(notice);
        } catch (DataIntegrityViolationException e) {
            // Başka bir kopya aynı anda yazdı; amaç zaten tekrar göndermemek
            log.debug("Bildirim kaydı zaten var: {}", notice.getAssetId());
        }
    }

    private static LicenseNotice toNotice(ApplicationAsset app, long daysLeft) {
        Person owner = app.getResponsibilityOwner();
        boolean realOwner = owner != null && !owner.isSystem();
        return new LicenseNotice(app.getId(), app.getAssetName(), app.getVendor(), app.getExpiryDate(), daysLeft,
                realOwner ? owner.getFullName() : null, realOwner ? owner.getEmail() : null);
    }
}
