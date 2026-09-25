package tr.com.nkolay.itam.model.dto;

import tr.com.nkolay.itam.enums.DependencyType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssetDependencyRequest(
        @NotNull(message = "{itam.validation.zorunlu}")
        Long parentAssetId,

        @NotNull(message = "{itam.validation.zorunlu}")
        Long childAssetId,

        @NotNull(message = "{itam.validation.zorunlu}")
        DependencyType dependencyType,

        @Size(max = 255, message = "{itam.validation.uzunluk}")
        String notes) {
}
