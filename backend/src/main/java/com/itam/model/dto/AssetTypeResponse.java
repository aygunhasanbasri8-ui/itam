package com.itam.model.dto;

import com.itam.enums.AssetKind;

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
