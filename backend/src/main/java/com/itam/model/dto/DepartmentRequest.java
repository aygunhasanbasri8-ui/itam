package com.itam.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 150, message = "{itam.validation.uzunluk}")
        String name,

        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 30, message = "{itam.validation.uzunluk}")
        String code,

        @NotNull(message = "{itam.validation.zorunlu}")
        Boolean active) {
}
