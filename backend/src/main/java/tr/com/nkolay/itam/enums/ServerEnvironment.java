package tr.com.nkolay.itam.enums;

/**
 * Sunucunun çalıştığı ortam. Filtreleme ve dashboard dağılımında kullanılır.
 * <p>
 * Değer adları PostgreSQL enum tipiyle birebir aynıdır; Hibernate NAMED_ENUM
 * eşlemesi isim üzerinden çalışır.
 */
public enum ServerEnvironment {
	DEV, TEST, UAT, PROD
}
