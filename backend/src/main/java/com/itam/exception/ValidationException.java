package com.itam.exception;

import java.util.List;

import com.itam.model.dto.ErrorDetail;

import lombok.Getter;

/**
 * Servis katmanı iş kuralı ihlallerini taşır.
 * <p>
 * Tek hata değil, hata LİSTESİ taşıması bilinçlidir: kullanıcıyı formu
 * defalarca göndermeye zorlamamak için tüm ihlaller tek seferde döner.
 */
@Getter
public class ValidationException extends RuntimeException {

	private final transient List<ErrorDetail> errors;

	public ValidationException(List<ErrorDetail> errors) {
		super(errors.size() + " doğrulama hatası");
		this.errors = List.copyOf(errors);
	}
}
