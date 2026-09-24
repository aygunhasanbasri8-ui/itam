package tr.com.nkolay.itam.model.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "{itam.validation.zorunlu}") String username,
        @NotBlank(message = "{itam.validation.zorunlu}") String password) {
}
