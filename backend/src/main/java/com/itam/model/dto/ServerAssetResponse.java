package com.itam.model.dto;

import java.time.OffsetDateTime;
import java.util.Map;

import com.itam.enums.AssetStatus;
import com.itam.enums.ServerEnvironment;

public record ServerAssetResponse(
        Long id,
        String assetName,
        AssetStatus status,
        String notes,
        PersonSummary responsibilityOwner,
        String domain,
        String hostname,
        String ipAddress,
        ServerEnvironment environment,
        String serverClass,
        String groupName,
        String groupInfo,
        String groupDesc,
        String infrastructureType,
        String os,
        boolean virtual,
        PersonSummary serviceOwner,
        PersonSummary appResponsible,
        PersonSummary osResponsible,
        Map<String, String> customFields,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
