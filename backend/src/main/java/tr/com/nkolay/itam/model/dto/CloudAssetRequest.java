package tr.com.nkolay.itam.model.dto;

import java.util.Map;

import tr.com.nkolay.itam.enums.CloudProvider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** @param responsibilityOwnerId boş geçilirse servis katmanı FIRMA kaydını atar */
public record CloudAssetRequest(
        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 200, message = "{itam.validation.uzunluk}")
        String assetName,

        String notes,

        Long responsibilityOwnerId,

        @NotNull(message = "{itam.validation.zorunlu}")
        CloudProvider cloudProvider,

        @Size(max = 80, message = "{itam.validation.uzunluk}")
        String instanceType,

        @Size(max = 60, message = "{itam.validation.uzunluk}")
        String region,

        @Size(max = 120, message = "{itam.validation.uzunluk}")
        String accountId,

        Map<String, String> customFields) {
}
