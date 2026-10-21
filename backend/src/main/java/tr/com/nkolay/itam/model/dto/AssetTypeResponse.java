package tr.com.nkolay.itam.model.dto;

import tr.com.nkolay.itam.enums.AssetKind;

public record AssetTypeResponse(
        String code,
        String displayName,
        AssetKind kind,
        String icon,
        String description,
        boolean system,
        boolean active,
        int sortOrder) {
}
