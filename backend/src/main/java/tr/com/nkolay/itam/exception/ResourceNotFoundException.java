package tr.com.nkolay.itam.exception;

/** İstenen kayıt bulunamadığında fırlatılır; 404 yanıtına çevrilir. */
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String kaynak, Object id) {
		super("%s bulunamadı: %s".formatted(kaynak, id));
	}
}
