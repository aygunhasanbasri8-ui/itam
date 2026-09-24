package tr.com.nkolay.itam.model.dto;

import java.time.OffsetDateTime;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.enums.ServiceCriticality;

public record BusinessServiceAssetResponse(
        Long id,
        String assetName,
        AssetStatus status,
        String notes,
        PersonSummary responsibilityOwner,
        ServiceCriticality criticality,
        String sla,
        PersonSummary serviceOwner,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
