package tr.com.nkolay.itam.enums;

/**
 * İki varlık arasındaki bağımlılığın türü.
 * RUNS_ON: uygulama → sunucu, USES: servis → uygulama.
 * <p>
 * Değer adları PostgreSQL enum tipiyle birebir aynıdır; Hibernate NAMED_ENUM
 * eşlemesi isim üzerinden çalışır.
 */
public enum DependencyType {
	RUNS_ON, USES, DEPENDS_ON
}
