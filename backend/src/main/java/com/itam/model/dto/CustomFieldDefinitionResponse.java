package com.itam.model.dto;

import java.time.OffsetDateTime;

import com.itam.enums.CustomFieldType;

public record CustomFieldDefinitionResponse(
        Long id,
        String assetTypeCode,
        String fieldKey,
        String label,
        CustomFieldType dataType,
        boolean showInList,
        boolean active,
        int sortOrder,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
