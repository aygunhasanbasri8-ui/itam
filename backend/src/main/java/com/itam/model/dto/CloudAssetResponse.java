package com.itam.model.dto;

import java.time.OffsetDateTime;
import java.util.Map;

import com.itam.enums.AssetStatus;
import com.itam.enums.CloudProvider;

public record CloudAssetResponse(
        Long id,
        String assetName,
        AssetStatus status,
        String notes,
        PersonSummary responsibilityOwner,
        CloudProvider cloudProvider,
        String instanceType,
        String region,
        String accountId,
        Map<String, String> customFields,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
