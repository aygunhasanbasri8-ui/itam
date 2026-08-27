package tr.com.nkolay.itam.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Özel doğrulayıcılar")
class ValidatorsTest {

	private final IpAddressValidator ip = new IpAddressValidator();

	private final HostnameValidator hostname = new HostnameValidator();

	@ParameterizedTest
	@ValueSource(strings = { "10.20.0.15", "0.0.0.0", "255.255.255.255",
			"2001:db8::1", "::1", "::ffff:192.168.1.1" })
	@DisplayName("geçerli IP adresleri kabul ediliyor")
	void gecerliIp(String deger) {
		assertThat(ip.isValid(deger, null)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "10.0.0.999", "256.1.1.1", "10.0.0", "abc", "10.0.0.1.1" })
	@DisplayName("geçersiz IP adresleri reddediliyor")
	void gecersizIp(String deger) {
		assertThat(ip.isValid(deger, null)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "prod-db-01", "srv1.nkolay.local", "a", "test-01.corp.tr" })
	@DisplayName("geçerli hostname'ler kabul ediliyor")
	void gecerliHostname(String deger) {
		assertThat(hostname.isValid(deger, null)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-basta-tire", "sonda-tire-", "iki..nokta", "boşluk var" })
	@DisplayName("geçersiz hostname'ler reddediliyor")
	void gecersizHostname(String deger) {
		assertThat(hostname.isValid(deger, null)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "   " })
	@DisplayName("boş değer doğrulayıcıyı geçer; zorunluluk @NotBlank işidir")
	void bosDegerGecerli(String deger) {
		assertThat(ip.isValid(deger, null)).isTrue();
		assertThat(hostname.isValid(deger, null)).isTrue();
	}
}
