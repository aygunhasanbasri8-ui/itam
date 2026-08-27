package tr.com.nkolay.itam.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tr.com.nkolay.itam.exception.ValidationException;
import tr.com.nkolay.itam.model.dto.ErrorDetail;

@DisplayName("ValidationCollector")
class ValidationCollectorTest {

	@Test
	@DisplayName("tüm hataları toplar, ilk hatada durmaz")
	void tumHatalariToplar() {
		ValidationCollector collector = ValidationCollector.create()
				.check(false, "hostname", "NotBlank", "Hostname zorunludur", "")
				.check(false, "ipAddress", "ValidIp", "Geçersiz IP", "10.0.0.999")
				.check(true, "os", "NotBlank", "İşletim sistemi zorunludur", "RHEL 9");

		assertThat(collector.errors()).hasSize(2)
				.extracting(ErrorDetail::field)
				.containsExactly("hostname", "ipAddress");
	}

	@Test
	@DisplayName("satır numarası içe aktarım hatalarına işleniyor")
	void satirNumarasiIsleniyor() {
		ValidationCollector collector = ValidationCollector.create()
				.forRow(7)
				.check(false, "hostname", "Duplicate",
						"Aynı hostname dosyada tekrar ediyor", "srv-01");

		assertThat(collector.errors().getFirst().row()).isEqualTo(7);
	}

	@Test
	@DisplayName("hata yoksa throwIfInvalid sessiz kalır")
	void hatasizDurum() {
		ValidationCollector.create().check(true, "x", "c", "m", null).throwIfInvalid();
	}

	@Test
	@DisplayName("hata varsa tek exception'da hepsi döner")
	void hataylaFirlatir() {
		assertThatThrownBy(() -> ValidationCollector.create()
				.check(false, "a", "c", "m", null)
				.check(false, "b", "c", "m", null)
				.throwIfInvalid())
				.isInstanceOf(ValidationException.class)
				.satisfies(ex -> assertThat(((ValidationException) ex).getErrors()).hasSize(2));
	}
}
