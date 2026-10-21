package com.itam.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Kod ve tür (kind) burada yok: her tipin kendi tablosu ve ekranı var, kodu
 * değiştirmek mevcut kayıtların bağını koparır. Yalnızca görünen bilgiler düzenleniyor.
 */
public record AssetTypeUpdateRequest(
        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 100, message = "{itam.validation.uzunluk}")
        String displayName,

        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 60, message = "{itam.validation.uzunluk}")
        String icon,

        @Size(max = 255, message = "{itam.validation.uzunluk}")
        String description,

        int sortOrder) {
}
