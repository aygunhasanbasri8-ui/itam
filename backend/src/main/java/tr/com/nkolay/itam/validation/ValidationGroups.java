package tr.com.nkolay.itam.validation;

/**
 * Oluşturma ve güncellemede farklı kurallar uygulamak için doğrulama grupları.
 * <p>
 * Örnek: hostname oluşturmada zorunlu, güncellemede değiştirilemez olduğu için
 * istekte hiç gönderilmeyebilir.
 * <p>
 * Controller'da: {@code @Validated(OnCreate.class) @RequestBody ...}
 */
public final class ValidationGroups {

	private ValidationGroups() {
	}

	public interface OnCreate {
	}

	public interface OnUpdate {
	}
}
