package com.itam.service.notification;

import java.time.LocalDate;

/** Kanallara giden bildirim; entity taşımıyor, kanal veriye uzanmasın. */
public record LicenseNotice(Long assetId, String assetName, String vendor, LocalDate expiryDate, long daysLeft,
                            String ownerName, String ownerEmail) {

    public String subject() {
        return "Lisans bitiyor: " + assetName + " (" + daysLeft + " gün)";
    }

    public String text() {
        return assetName + (vendor == null ? "" : " / " + vendor)
                + " lisansı " + expiryDate + " tarihinde bitiyor, " + daysLeft + " gün kaldı."
                + (ownerName == null ? "" : " Sorumlu: " + ownerName + ".");
    }
}
