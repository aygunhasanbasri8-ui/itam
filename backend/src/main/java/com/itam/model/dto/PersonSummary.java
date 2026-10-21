package com.itam.model.dto;

/** Liste ve detay yanıtlarında kişi referanslarının basılan hali — tam Person yerine sadece bunlar gerekiyor. */
public record PersonSummary(Long id, String fullName) {
}
