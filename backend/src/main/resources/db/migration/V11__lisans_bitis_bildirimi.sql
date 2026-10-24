-- Gönderilen lisans bitiş bildirimleri. Aynı lisans + bitiş tarihi + eşik için ikinci
-- bildirim gitmesin diye tutuluyor (uygulama yeniden başlasa da, birden çok kopya
-- çalışsa da). Bitiş tarihi güncellenirse yeni tarih için bildirimler yeniden başlar.
CREATE SEQUENCE license_expiry_notice_seq START WITH 51 INCREMENT BY 50;

CREATE TABLE license_expiry_notice (
    id           BIGINT      PRIMARY KEY,
    asset_id     BIGINT      NOT NULL REFERENCES base_asset (id) ON DELETE CASCADE,
    expiry_date  DATE        NOT NULL,
    days_before  INTEGER     NOT NULL,
    sent_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_license_expiry_notice UNIQUE (asset_id, expiry_date, days_before)
);
