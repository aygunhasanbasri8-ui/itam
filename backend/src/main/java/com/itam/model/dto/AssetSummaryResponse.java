package com.itam.model.dto;

import java.time.OffsetDateTime;

import com.itam.enums.AssetStatus;

/** Tipten bağımsız listelerin (arama, tüm varlıklar, son eklenenler) satırı. */
public record AssetSummaryResponse(
        Long id,
        String assetName,
        String assetTypeCode,
        AssetStatus status,
        PersonSummary responsibilityOwner,
        OffsetDateTime createdAt) {
}
