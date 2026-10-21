package tr.com.nkolay.itam.model.dto;

import tr.com.nkolay.itam.enums.UserRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Yeni kullanıcı. Kullanıcı adı sonradan değiştirilemez: denetim kayıtlarında bu adla geçiyor. */
public record AppUserRequest(
        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(max = 80, message = "{itam.validation.uzunluk}")
        String username,

        // BCrypt 72 bayttan sonrasını sessizce kesiyor, üst sınır bu yüzden
        @NotBlank(message = "{itam.validation.zorunlu}")
        @Size(min = 8, max = 72, message = "{itam.validation.sifreUzunlugu}")
        String password,

        @NotNull(message = "{itam.validation.zorunlu}")
        UserRole role,

        Long personId) {
}
