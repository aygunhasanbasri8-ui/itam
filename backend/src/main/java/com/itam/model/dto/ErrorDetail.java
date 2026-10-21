package com.itam.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Tek bir doğrulama hatası.
 * <p>
 * Tek kayıt, toplu işlem ve dosya içe aktarımı bu aynı yapıyı kullanır;
 * istemci tarafında tek bir hata gösterim bileşeni yeterli olur.
 *
 * @param row           toplu işlem/içe aktarımda satır numarası, tek kayıtta null
 * @param field         hatalı alan adı
 * @param code          makine tarafından okunabilir hata kodu (NotBlank, ValidIp)
 * @param message       kullanıcıya gösterilecek metin
 * @param rejectedValue reddedilen değer
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(
		Integer row,
		String field,
		String code,
		String message,
		Object rejectedValue) {

	public static ErrorDetail of(String field, String code, String message, Object rejected) {
		return new ErrorDetail(null, field, code, message, rejected);
	}

	public static ErrorDetail ofRow(int row, String field, String code,
			String message, Object rejected) {
		return new ErrorDetail(row, field, code, message, rejected);
	}
}
