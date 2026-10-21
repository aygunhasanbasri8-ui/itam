package tr.com.nkolay.itam.model.dto;

import java.time.OffsetDateTime;

import tr.com.nkolay.itam.enums.UserRole;

public record AppUserResponse(
        Long id,
        String username,
        UserRole role,
        PersonSummary person,
        boolean active,
        OffsetDateTime lastLoginAt,
        OffsetDateTime createdAt) {
}
