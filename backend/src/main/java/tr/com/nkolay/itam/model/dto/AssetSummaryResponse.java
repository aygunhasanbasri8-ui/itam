package tr.com.nkolay.itam.model.dto;

import java.time.OffsetDateTime;

import tr.com.nkolay.itam.enums.AssetStatus;

/** Tipten bağımsız listelerin (arama, tüm varlıklar, son eklenenler) satırı. */
public record AssetSummaryResponse(
        Long id,
        String assetName,
        String assetTypeCode,
        AssetStatus status,
        PersonSummary responsibilityOwner,
        OffsetDateTime createdAt) {
}
