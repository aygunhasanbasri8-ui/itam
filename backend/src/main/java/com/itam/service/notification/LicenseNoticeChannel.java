package com.itam.service.notification;

/** Bildirim kanalı. Yapılandırılmamış kanal devre dışı sayılır. */
public interface LicenseNoticeChannel {

    String name();

    boolean isConfigured();

    void send(LicenseNotice notice) throws Exception;
}
