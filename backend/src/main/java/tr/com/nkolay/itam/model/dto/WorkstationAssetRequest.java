package tr.com.nkolay.itam.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * assetType burada da yok, aynı sebepten: workstation_asset her zaman
 * "WORKSTATION" tipine bağlı, servis katmanı sabit atıyor.
 *
 * @param responsibilityOwnerId boş geçilirse servis katmanı FIRMA kaydını atar
 * @param assignedPersonId      zimmetli kullanıcı, opsiyonel — depoda bekleyen cihazın sahibi olmayabilir
 */
public record WorkstationAssetRequest(
        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 200, message = "{itam.validation.uzunluk}")
        String assetName,

        String notes,

        Long responsibilityOwnerId,

        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 100, message = "{itam.validation.uzunluk}")
        String serialNumber,

        @Size(max = 150, message = "{itam.validation.uzunluk}")
        String model,

        @Size(max = 150, message = "{itam.validation.uzunluk}")
        String cpu,

        @Positive
        Integer ramGb,

        @Positive
        Integer diskGb,

        Long assignedPersonId,
        Long departmentId) {
}
