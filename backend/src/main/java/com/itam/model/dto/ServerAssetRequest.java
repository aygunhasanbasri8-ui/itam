package com.itam.model.dto;

import java.util.Map;

import com.itam.enums.ServerEnvironment;
import com.itam.validation.ValidHostname;
import com.itam.validation.ValidIpAddress;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Sunucu ekleme/güncelleme isteği.
 * <p>
 * assetType kasten burada yok: server_asset her zaman "SERVER" tipine bağlı,
 * bunu istemciye seçtirmenin bir faydası yok, servis katmanı sabit atıyor.
 * status da burada yok — durum değişikliği ileride toplu durum güncelleme
 * uç noktasından yapılacak, tekil kayıt güncellemesi bunu kapsamıyor.
 *
 * @param responsibilityOwnerId boş geçilirse servis katmanı FIRMA kaydını atar
 */
public record ServerAssetRequest(
        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 200, message = "{itam.validation.uzunluk}")
        String assetName,

        String notes,

        Long responsibilityOwnerId,

        @Size(max = 120, message = "{itam.validation.uzunluk}")
        String domain,

        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 150, message = "{itam.validation.uzunluk}")
        @ValidHostname
        String hostname,

        @ValidIpAddress
        String ipAddress,

        @NotNull(message = "{itam.validation.zorunlu}")
        ServerEnvironment environment,

        @Size(max = 80, message = "{itam.validation.uzunluk}")
        String serverClass,

        @Size(max = 120, message = "{itam.validation.uzunluk}")
        String groupName,

        @Size(max = 255, message = "{itam.validation.uzunluk}")
        String groupInfo,

        String groupDesc,

        @Size(max = 80, message = "{itam.validation.uzunluk}")
        String infrastructureType,

        @Size(max = 120, message = "{itam.validation.uzunluk}")
        String os,

        boolean virtual,

        Long serviceOwnerId,
        Long appResponsibleId,
        Long osResponsibleId,

        Map<String, String> customFields) {
}
