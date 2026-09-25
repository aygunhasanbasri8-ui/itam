package tr.com.nkolay.itam.model.dto;

import tr.com.nkolay.itam.enums.CustomFieldType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Yeni özel alan tanımı. assetTypeCode/fieldKey/dataType oluşturulduktan sonra
 * değiştirilemez — asset'lerin custom_fields JSONB'inde bu anahtarla değer
 * birikmiş olabilir, tipini sonradan değiştirmek eski verileri anlamsız kılar.
 */
public record CustomFieldDefinitionRequest(
        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 40, message = "{itam.validation.uzunluk}")
        String assetTypeCode,

        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 60, message = "{itam.validation.uzunluk}")
        @Pattern(regexp = "[a-zA-Z][a-zA-Z0-9_]*", message = "{itam.validation.alanAnahtari}")
        String fieldKey,

        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 120, message = "{itam.validation.uzunluk}")
        String label,

        @NotNull(message = "{itam.validation.zorunlu}")
        CustomFieldType dataType,

        boolean showInList,

        int sortOrder) {
}
