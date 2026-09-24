package tr.com.nkolay.itam.model.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** @param responsibilityOwnerId boş geçilirse servis katmanı FIRMA kaydını atar */
public record ApplicationAssetRequest(
        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 200, message = "{itam.validation.uzunluk}")
        String assetName,

        String notes,

        Long responsibilityOwnerId,

        @Size(max = 60, message = "{itam.validation.uzunluk}")
        String version,

        @Size(max = 80, message = "{itam.validation.uzunluk}")
        String licenseType,

        @Positive
        Integer licenseCount,

        @Size(max = 150, message = "{itam.validation.uzunluk}")
        String vendor,

        LocalDate expiryDate,

        @Size(max = 400, message = "{itam.validation.uzunluk}")
        String installPath) {
}
