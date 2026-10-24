package com.itam.service.notification;

import java.time.LocalDate;

/**
 * Bitişi yaklaşan uygulama lisansları için 30/7/1 gün kala (ayarlanabilir) haber verir.
 * Görev saatte bir çalışır; her eşik için bir kez gönderilir.
 */
public interface LicenseExpiryNotificationService {

    /** Bir tur; gönderilen bildirim sayısını döner. Test ve elle tetikleme için gün dışarıdan. */
    int runOnce(LocalDate today);
}
