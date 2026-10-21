package com.itam;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Katman zincirini koruyan test.
 * <p>
 * Katman-önce paketlemenin bedeli, hiçbir sınıfın package-private kalamamasıdır:
 * ServerService ile ServerRepository farklı paketlerde olduğu için repository
 * public olmak zorundadır ve derleyici kimin ona uzandığını denetleyemez.
 * Bu test, derleyicinin yapamadığını kaynak dosyaların import satırlarını
 * okuyarak yapar.
 * <p>
 * <b>Sınırı:</b> yalnızca import satırlarına bakar. Tam nitelikli inline kullanım
 * ({@code com.itam.repository.X x = ...}) yakalanmaz. ArchUnit bunu
 * bytecode seviyesinde yapar; bağımlılık eklemeden değerin büyük kısmı alınıyor.
 * <p>
 * Spring bağlamı yoktur — yalnızca dosya okur.
 */
@DisplayName("Katman zinciri")
class ArchitectureTest {

	private static final Path BACKEND = Path.of("src/main/java/com/itam");

	private static final String PKG = "com.itam.";

	/**
	 * Repository'ye yalnızca servis ve güvenlik katmanı dokunur. Bir controller ya da
	 * mapper repository'ye uzanırsa, bir tablonun okunma kuralı (soft delete filtresi,
	 * sıralama) iki ayrı yerde yaşamaya başlar.
	 */
	@Test
	@DisplayName("repository'ye yalnızca servis ve güvenlik dokunur")
	void repositoryErisimiSinirli() {
		assertNoImport(PKG + "repository.",
				path -> !path.contains("/service/")
						&& !path.contains("/security/")
						&& !path.contains("/repository/"));
	}

	/** Controller yalnızca servis arayüzlerine konuşur; repository ve mapper görmez. */
	@Test
	@DisplayName("controller yalnızca servise konuşur")
	void controllerZinciri() {
		Predicate<String> controllers = path -> path.contains("/controller/");
		assertNoImport(PKG + "repository.", controllers);
		assertNoImport(PKG + "mapper.", controllers);
		// Arayüz yerine gerçekleştirime bağlanmak, servisin içi değişince controller'ı açar.
		assertNoImport(".service.", controllers, "impl.");
	}

	/**
	 * Servis HTTP bilmez. Bir controller "şu IOException'ı ben çevirmeyeyim" deyip
	 * MultipartFile'ı servise geçerse, servis bir web tipine bağımlı olur ve HTTP
	 * dışından çağrılamaz hale gelir.
	 */
	@Test
	@DisplayName("servis web tiplerini görmez")
	void servisWebBilmez() {
		Predicate<String> services = path -> path.contains("/service/");
		assertNoImport("org.springframework.web.multipart.", services);
		assertNoImport("jakarta.servlet.", services);
	}

	/** Mapper saf çevrimdir: veriye gitmez, iş kuralı işletmez. */
	@Test
	@DisplayName("mapper veriye ve iş kuralına uzanmaz")
	void mapperSafCevrim() {
		Predicate<String> mappers = path -> path.contains("/mapper/");
		assertNoImport(PKG + "repository.", mappers);
		assertNoImport(PKG + "service.", mappers);
	}

	/**
	 * DTO katmanı entity'den tamamen bağımsızdır. Bir DTO entity import etmeye
	 * başlarsa, şema değişikliği API sözleşmesini habersiz kırar.
	 * <p>
	 * Bu kural, entity'den DTO üreten {@code static from()} desenini yasaklar;
	 * çevrim mapper/ katmanında yapılır.
	 */
	@Test
	@DisplayName("DTO entity'ye bağımlı olmaz")
	void dtoEntityBilmez() {
		assertNoImport(PKG + "model.entity.", path -> path.contains("/model/dto/"));
	}

	/**
	 * Şemayı yalnızca Flyway yönetir (ddl-auto: validate). Java kaynağında DDL
	 * geçmemelidir. Import kontrolü bunu göremez — SQL bir metin, katman değil;
	 * bu yüzden test tüm satırlara bakıyor.
	 */
	@Test
	@DisplayName("Java kaynağında DDL çalıştırılmaz")
	void ddlYalnizcaFlywayda() {
		assertNoLine("create table", path -> true);
		assertNoLine("alter table", path -> true);
		assertNoLine("drop table", path -> true);
	}

	// --- yardımcılar ---------------------------------------------------------

	private void assertNoImport(String forbidden, Predicate<String> appliesTo) {
		assertNoImport(forbidden, appliesTo, null);
	}

	/**
	 * @param mustAlsoContain null değilse satır hem forbidden hem bunu içermeli;
	 *                        "service importu serbest ama impl olanı değil" gibi kurallar için
	 */
	private void assertNoImport(String forbidden, Predicate<String> appliesTo,
			String mustAlsoContain) {
		List<String> violations = sourceFiles()
				.filter(path -> appliesTo.test(normalize(path)))
				.flatMap(path -> lines(path).stream()
						.filter(line -> line.startsWith("import"))
						.filter(line -> line.contains(forbidden))
						.filter(line -> mustAlsoContain == null || line.contains(mustAlsoContain))
						.map(line -> normalize(path) + "  ->  " + line.trim()))
				.toList();

		assertThat(violations)
				.as("Katman zinciri ihlali: '%s' importu bu dosyalarda olamaz", forbidden)
				.isEmpty();
	}

	private void assertNoLine(String forbidden, Predicate<String> appliesTo) {
		List<String> violations = sourceFiles()
				.filter(path -> appliesTo.test(normalize(path)))
				.flatMap(path -> lines(path).stream()
						.filter(line -> line.toLowerCase().contains(forbidden))
						.map(line -> normalize(path) + "  ->  " + line.trim()))
				.toList();

		assertThat(violations)
				.as("Kural ihlali: '%s' Java kaynağında geçemez, şema Flyway'in", forbidden)
				.isEmpty();
	}

	private Stream<Path> sourceFiles() {
		try {
			return Files.walk(BACKEND)
					.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().endsWith(".java"));
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private List<String> lines(Path path) {
		try {
			return Files.readAllLines(path);
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private String normalize(Path path) {
		return path.toString().replace('\\', '/');
	}
}
