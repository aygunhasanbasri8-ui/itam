package tr.com.nkolay.itam.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PersonRequest(
        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 150, message = "{itam.validation.uzunluk}")
        String fullName,

        @Size(max = 40, message = "{itam.validation.uzunluk}")
        String code,

        @Email
        @Size(max = 180, message = "{itam.validation.uzunluk}")
        String email,

        @Size(max = 120, message = "{itam.validation.uzunluk}")
        String title,

        @Size(max = 30, message = "{itam.validation.uzunluk}")
        String phone,

        Long departmentId,

        @NotNull(message = "{itam.validation.zorunlu}")
        Boolean active) {
}
