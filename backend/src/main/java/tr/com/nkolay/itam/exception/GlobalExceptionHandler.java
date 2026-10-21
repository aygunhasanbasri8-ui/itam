package tr.com.nkolay.itam.exception;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import tr.com.nkolay.itam.model.dto.ErrorDetail;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Tüm hataları RFC 7807 Problem Details formatına çeviren merkezi nokta.
 * <p>
 * Doğrulama hataları yanıta "errors" dizisi olarak eklenir; istemci tarafı
 * tek kayıt, toplu işlem ve içe aktarım için aynı bileşeni kullanabilir.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final String BASE_URI = "https://itam.nkolay.com.tr/errors/";

	/** {@code @Valid} ile işaretlenmiş istek gövdesi doğrulamadan geçemedi. */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleBodyValidation(MethodArgumentNotValidException ex) {
		List<ErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> ErrorDetail.of(
						fe.getField(),
						fe.getCode(),
						fe.getDefaultMessage(),
						maskSecret(fe.getField(), fe.getRejectedValue())))
				.toList();
		return build(HttpStatus.BAD_REQUEST, "validation", "Doğrulama hatası", errors);
	}

	/** {@code @RequestParam} veya {@code @PathVariable} doğrulamadan geçemedi. */
	@ExceptionHandler(ConstraintViolationException.class)
	ProblemDetail handleParamValidation(ConstraintViolationException ex) {
		List<ErrorDetail> errors = ex.getConstraintViolations().stream()
				.map(cv -> ErrorDetail.of(
						cv.getPropertyPath().toString(),
						cv.getConstraintDescriptor().getAnnotation()
								.annotationType().getSimpleName(),
						cv.getMessage(),
						cv.getInvalidValue()))
				.toList();
		return build(HttpStatus.BAD_REQUEST, "validation", "Doğrulama hatası", errors);
	}

	/** Servis katmanı iş kuralı ihlalleri (ValidationCollector kaynaklı). */
	@ExceptionHandler(ValidationException.class)
	ProblemDetail handleBusinessValidation(ValidationException ex) {
		return build(HttpStatus.BAD_REQUEST, "validation", "Doğrulama hatası", ex.getErrors());
	}

	/** Kullanıcı adı/şifre yanlış — AuthService fırlatır. */
	@ExceptionHandler(BadCredentialsException.class)
	ProblemDetail handleBadCredentials(BadCredentialsException ex) {
		ProblemDetail pd = base(HttpStatus.UNAUTHORIZED, "unauthorized", "Kimlik doğrulama başarısız");
		pd.setDetail("Kullanıcı adı veya şifre hatalı.");
		return pd;
	}

	/** Geçersiz parametre (ör. export'ta bilinmeyen kolon kodu) — kod hatası değil, istemci hatası. */
	@ExceptionHandler(IllegalArgumentException.class)
	ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
		ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "invalid-request", "Geçersiz istek");
		pd.setDetail(ex.getMessage());
		return pd;
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	ProblemDetail handleNotFound(ResourceNotFoundException ex) {
		ProblemDetail pd = base(HttpStatus.NOT_FOUND, "not-found", "Kayıt bulunamadı");
		pd.setDetail(ex.getMessage());
		return pd;
	}

	/**
	 * Veritabanı kısıtı ihlali. Tekillik indeksleri ve check constraint'ler buraya
	 * düşer; uygulama katmanı atlansa bile son savunma hattı devrededir.
	 */

	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail handleIntegrity(DataIntegrityViolationException ex) {
		log.warn("Veri bütünlüğü ihlali: {}", ex.getMostSpecificCause().getMessage());
		ProblemDetail pd = base(HttpStatus.CONFLICT, "conflict", "Kayıt çakışması");
		pd.setDetail("Girilen değerlerden biri mevcut bir kayıtla çakışıyor "
				+ "veya bir veritabanı kuralını ihlal ediyor.");
		return pd;
	}

	/** Bozuk JSON ya da enum'a uymayan bir değer; Jackson'ın iç mesajını dışarı vermiyoruz. */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex) {
		ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "invalid-request", "Geçersiz istek");
		pd.setDetail("İstek gövdesi okunamadı; JSON biçimini ve alan değerlerini kontrol edin.");
		return pd;
	}

	/** /servers/abc gibi sayı beklenen yerde metin gelmesi. */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "invalid-request", "Geçersiz istek");
		pd.setDetail("'%s' parametresi için geçersiz değer: %s".formatted(ex.getName(), ex.getValue()));
		return pd;
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail handleUnexpected(Exception ex) {
		// Bilinmeyen adres, desteklenmeyen metot, eksik parametre gibi Spring MVC
		// hataları kendi durum kodunu taşıyor; bunları 500'e çevirmek istemciyi yanıltır.
		if (ex instanceof ErrorResponse errorResponse) {
			HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
			ProblemDetail pd = base(status, "invalid-request", status.getReasonPhrase());
			pd.setDetail(errorResponse.getBody().getDetail());
			return pd;
		}
		log.error("Beklenmeyen hata", ex);
		ProblemDetail pd = base(HttpStatus.INTERNAL_SERVER_ERROR, "internal",
				"Beklenmeyen bir hata oluştu");
		pd.setDetail("İşlem tamamlanamadı. Sorun sürerse sistem yöneticisine başvurun.");
		return pd;
	}

	private ProblemDetail build(HttpStatus status, String type, String title,
			List<ErrorDetail> errors) {
		ProblemDetail pd = base(status, type, title);
		pd.setDetail("%d alan doğrulamadan geçemedi".formatted(errors.size()));
		pd.setProperty("errors", errors);
		return pd;
	}

	// Şifre gibi alanlar hata yanıtında ve loglarda geri basılmasın
	private static Object maskSecret(String field, Object rejectedValue) {
		return field.toLowerCase().contains("password") ? null : rejectedValue;
	}

	private ProblemDetail base(HttpStatus status, String type, String title) {
		ProblemDetail pd = ProblemDetail.forStatus(status);
		pd.setType(URI.create(BASE_URI + type));
		pd.setTitle(title);
		pd.setProperty("timestamp", OffsetDateTime.now());
		return pd;
	}
}
