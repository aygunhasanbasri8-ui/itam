package com.itam.model.dto;

import java.time.OffsetDateTime;

public record DepartmentResponse(
        Long id,
        String name,
        String code,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
