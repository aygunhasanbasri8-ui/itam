package com.itam.model.dto;

import com.itam.enums.UserRole;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** password boş gelirse mevcut şifre korunur; doluysa admin şifreyi sıfırlamış olur. */
public record AppUserUpdateRequest(
        @Size(min = 8, max = 72, message = "{itam.validation.sifreUzunlugu}")
        String password,

        @NotNull(message = "{itam.validation.zorunlu}")
        UserRole role,

        Long personId,

        @NotNull(message = "{itam.validation.zorunlu}")
        Boolean active) {
}
