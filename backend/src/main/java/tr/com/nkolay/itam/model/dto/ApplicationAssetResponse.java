package tr.com.nkolay.itam.model.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

import tr.com.nkolay.itam.enums.AssetStatus;

public record ApplicationAssetResponse(
        Long id,
        String assetName,
        AssetStatus status,
        String notes,
        PersonSummary responsibilityOwner,
        String version,
        String licenseType,
        Integer licenseCount,
        String vendor,
        LocalDate expiryDate,
        String installPath,
        Map<String, String> customFields,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
