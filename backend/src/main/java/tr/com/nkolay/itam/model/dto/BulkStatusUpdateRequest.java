package tr.com.nkolay.itam.model.dto;

import java.util.List;

import tr.com.nkolay.itam.enums.AssetStatus;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record BulkStatusUpdateRequest(
        @NotEmpty(message = "{itam.validation.zorunlu}")
        List<Long> assetIds,

        @NotNull(message = "{itam.validation.zorunlu}")
        AssetStatus status) {
}
