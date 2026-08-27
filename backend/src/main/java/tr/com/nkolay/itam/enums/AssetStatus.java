package tr.com.nkolay.itam.enums;

/**
 * Varlığın yaşam döngüsü durumu. RETIRED = hizmet dışı ama envanterde kayıtlı;
 * is_deleted bayrağından farklıdır.
 * <p>
 * Değer adları PostgreSQL enum tipiyle birebir aynıdır; Hibernate NAMED_ENUM
 * eşlemesi isim üzerinden çalışır.
 */
public enum AssetStatus {
	ACTIVE, RETIRED, MAINTENANCE
}
