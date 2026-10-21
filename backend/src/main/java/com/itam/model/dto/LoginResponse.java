package com.itam.model.dto;

public record LoginResponse(String token, String tokenType, String username, String role) {
}
