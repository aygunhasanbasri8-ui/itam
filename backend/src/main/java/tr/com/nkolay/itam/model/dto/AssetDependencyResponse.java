package tr.com.nkolay.itam.model.dto;

import java.time.OffsetDateTime;

import tr.com.nkolay.itam.enums.DependencyType;

public record AssetDependencyResponse(
        Long id,
        Long parentAssetId,
        String parentAssetName,
        Long childAssetId,
        String childAssetName,
        DependencyType dependencyType,
        String notes,
        OffsetDateTime createdAt,
        String createdBy) {
}
