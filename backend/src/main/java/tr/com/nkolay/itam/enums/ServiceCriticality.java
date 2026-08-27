package tr.com.nkolay.itam.enums;

/**
 * İş servisinin kritiklik seviyesi.
 * <p>
 * Değer adları PostgreSQL enum tipiyle birebir aynıdır; Hibernate NAMED_ENUM
 * eşlemesi isim üzerinden çalışır.
 */
public enum ServiceCriticality {
	HIGH, MID, LOW
}
