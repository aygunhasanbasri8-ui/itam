package tr.com.nkolay.itam.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import tr.com.nkolay.itam.exception.ValidationException;
import tr.com.nkolay.itam.model.dto.ErrorDetail;


public class ValidationCollector {

	private final List<ErrorDetail> errors = new ArrayList<>();

	private Integer currentRow;

	private ValidationCollector() {
	}

	public static ValidationCollector create() {
		return new ValidationCollector();
	}

	/** İzleyen tüm hataları bu satır numarasıyla işaretler (içe aktarım için). */
	public ValidationCollector forRow(int row) {
		this.currentRow = row;
		return this;
	}

	public ValidationCollector add(String field, String code, String message, Object rejected) {
		errors.add(currentRow == null
				? ErrorDetail.of(field, code, message, rejected)
				: ErrorDetail.ofRow(currentRow, field, code, message, rejected));
		return this;
	}

	/** Koşul false ise hata ekler. Okunabilirlik için: check(ip != null, ...) */
	public ValidationCollector check(boolean valid, String field, String code,
			String message, Object rejected) {
		if (!valid) {
			add(field, code, message, rejected);
		}
		return this;
	}

	/**
	 * Koşulu tembel değerlendirir. Veritabanına giden kontrolleri gereksiz yere
	 * çalıştırmamak için kullanılır.
	 */
	public ValidationCollector checkLazy(BooleanSupplier valid, String field, String code,
			String message, Object rejected) {
		if (!valid.getAsBoolean()) {
			add(field, code, message, rejected);
		}
		return this;
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	public List<ErrorDetail> errors() {
		return List.copyOf(errors);
	}

	/** Hata varsa tek seferde fırlatır; yoksa hiçbir şey yapmaz. */
	public void throwIfInvalid() {
		if (hasErrors()) {
			throw new ValidationException(errors);
		}
	}
}
