package tr.com.nkolay.itam.model.dto;

import java.time.OffsetDateTime;

public record PersonResponse(
        Long id,
        String fullName,
        String code,
        String email,
        String title,
        String phone,
        DepartmentSummary department,
        boolean system,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
