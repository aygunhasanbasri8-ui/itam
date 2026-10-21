package com.itam.model.dto;

import java.time.OffsetDateTime;
import java.util.Map;

import com.itam.enums.AssetStatus;

public record WorkstationAssetResponse(
        Long id,
        String assetName,
        AssetStatus status,
        String notes,
        PersonSummary responsibilityOwner,
        String serialNumber,
        String model,
        String cpu,
        Integer ramGb,
        Integer diskGb,
        PersonSummary assignedPerson,
        DepartmentSummary department,
        Map<String, String> customFields,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
