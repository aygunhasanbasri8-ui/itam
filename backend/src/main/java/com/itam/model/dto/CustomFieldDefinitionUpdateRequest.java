package com.itam.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** assetTypeCode/fieldKey/dataType burada yok — bkz. CustomFieldDefinitionRequest javadoc'u. */
public record CustomFieldDefinitionUpdateRequest(
        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 120, message = "{itam.validation.uzunluk}")
        String label,

        boolean showInList,

        boolean active,

        int sortOrder) {
}
