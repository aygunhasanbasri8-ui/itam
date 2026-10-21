package tr.com.nkolay.itam.model.dto;

import java.time.OffsetDateTime;

import tr.com.nkolay.itam.enums.AssetStatus;

/** Tipten bağımsız asset aramasının satırı; seçim kutuları için bu kadarı yetiyor. */
public record AssetSummaryResponse(
        Long id,
        String assetName,
        String assetTypeCode,
        AssetStatus status,
        OffsetDateTime createdAt) {
}
