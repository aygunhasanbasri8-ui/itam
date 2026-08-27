package tr.com.nkolay.itam.enums;

/**
 * K5 kararı: iki rol. ADMIN tam yetkili, USER yalnızca görüntüleme,
 * filtreleme ve dışa aktarım yapabilir.
 * <p>
 * Değer adları PostgreSQL enum tipiyle birebir aynıdır; Hibernate NAMED_ENUM
 * eşlemesi isim üzerinden çalışır.
 */
public enum UserRole {
	ADMIN, USER
}
