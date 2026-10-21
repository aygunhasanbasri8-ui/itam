package com.itam.model.dto;

import java.util.Map;

import com.itam.enums.ServiceCriticality;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param responsibilityOwnerId boş geçilirse servis katmanı FIRMA kaydını atar
 * @param criticality           boş geçilirse MID (entity varsayılanıyla aynı)
 */
public record BusinessServiceAssetRequest(
        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 200, message = "{itam.validation.uzunluk}")
        String assetName,

        String notes,

        Long responsibilityOwnerId,

        ServiceCriticality criticality,

        @Size(max = 120, message = "{itam.validation.uzunluk}")
        String sla,

        Long serviceOwnerId,

        Map<String, String> customFields) {
}
