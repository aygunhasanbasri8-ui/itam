package com.itam.model.dto;

import java.time.OffsetDateTime;
import java.util.Map;

import com.itam.enums.AssetStatus;
import com.itam.enums.ServiceCriticality;

public record BusinessServiceAssetResponse(
        Long id,
        String assetName,
        AssetStatus status,
        String notes,
        PersonSummary responsibilityOwner,
        ServiceCriticality criticality,
        String sla,
        PersonSummary serviceOwner,
        Map<String, String> customFields,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
