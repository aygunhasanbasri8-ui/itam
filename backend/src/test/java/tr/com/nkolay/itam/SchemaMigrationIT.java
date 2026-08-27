package tr.com.nkolay.itam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** Faz 0 kabul testi: Flyway şemayı eksiksiz uyguluyor mu? */
@DisplayName("V1 şema migration")
class SchemaMigrationIT extends AbstractIntegrationTest {

	@Autowired
	JdbcTemplate jdbc;

	@Test
	@DisplayName("beklenen tüm tablolar oluşuyor")
	void tablolarOlusuyor() {
		List<String> tablolar = jdbc.queryForList(
				"SELECT table_name FROM information_schema.tables "
						+ "WHERE table_schema = 'public' ORDER BY table_name",
				String.class);

		assertThat(tablolar).contains(
				"app_user", "application_asset", "asset_dependency", "asset_type_def",
				"base_asset", "business_service_asset", "cloud_asset", "custom_field_def",
				"department", "person", "revinfo", "server_asset", "workstation_asset");
	}

	@Test
	@DisplayName("sekiz enum tipi tanımlı")
	void enumTipleriTanimli() {
		List<String> enumlar = jdbc.queryForList(
				"SELECT typname FROM pg_type WHERE typtype = 'e' ORDER BY typname", String.class);

		assertThat(enumlar).containsExactlyInAnyOrder(
				"asset_kind", "asset_status", "cloud_provider", "custom_field_type",
				"dependency_type", "server_environment", "service_criticality", "user_role");
	}

	@Test
	@DisplayName("çekirdek beş asset tipi seed ediliyor")
	void assetTipleriSeedEdiliyor() {
		Integer adet = jdbc.queryForObject(
				"SELECT count(*) FROM asset_type_def WHERE is_system = TRUE", Integer.class);

		assertThat(adet).isEqualTo(5);
	}

	@Test
	@DisplayName("Firma sistem kaydı oluşuyor")
	void firmaKaydiOlusuyor() {
		Boolean sistemKaydi = jdbc.queryForObject(
				"SELECT is_system FROM person WHERE code = 'FIRMA'", Boolean.class);

		assertThat(sistemKaydi).isTrue();
	}

	@Test
	@DisplayName("sequence'ler seed kayıtlarıyla çakışmayacak şekilde başlıyor")
	void sequenceBaslangicDogru() {
		Long ilkDeger = jdbc.queryForObject("SELECT nextval('person_seq')", Long.class);

		// START WITH 51: Firma kaydının id = 1 değeriyle çakışmaz
		assertThat(ilkDeger).isEqualTo(51L);
	}

	@Test
	@DisplayName("geçersiz enum değeri veritabanı seviyesinde reddediliyor")
	void gecersizEnumReddediliyor() {
		jdbc.update("INSERT INTO base_asset (id, asset_name, asset_kind, asset_type, "
				+ "responsibility_owner_id) VALUES (9001, 'test', 'SERVER', 'SERVER', 1)");

		assertThat(catchThrowable(() -> jdbc.update(
				"INSERT INTO server_asset (asset_id, hostname, environment) "
						+ "VALUES (9001, 'test-host', 'STAGING')")))
				.isNotNull();
	}
}
