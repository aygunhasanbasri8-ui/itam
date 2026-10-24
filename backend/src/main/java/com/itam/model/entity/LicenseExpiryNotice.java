package com.itam.model.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Bir lisans için gönderilmiş bitiş bildirimi; tekrar göndermemek için tutuluyor. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "license_expiry_notice")
public class LicenseExpiryNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "license_expiry_notice_seq")
    @SequenceGenerator(name = "license_expiry_notice_seq", sequenceName = "license_expiry_notice_seq",
            allocationSize = 50)
    private Long id;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "days_before", nullable = false)
    private int daysBefore;

    @Column(name = "sent_at", nullable = false)
    private OffsetDateTime sentAt;

    public LicenseExpiryNotice(Long assetId, LocalDate expiryDate, int daysBefore, OffsetDateTime sentAt) {
        this.assetId = assetId;
        this.expiryDate = expiryDate;
        this.daysBefore = daysBefore;
        this.sentAt = sentAt;
    }
}
