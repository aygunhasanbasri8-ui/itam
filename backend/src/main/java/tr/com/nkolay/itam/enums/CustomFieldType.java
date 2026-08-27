package tr.com.nkolay.itam.enums;

/**
 * Kullanıcı tanımlı alanın veri tipi.
 *
 * SELECT, PERSON_REF ve ASSET_REF sonraki faz kapsamındadır (K2 kararı).
 * <p>
 * Değer adları PostgreSQL enum tipiyle birebir aynıdır; Hibernate NAMED_ENUM
 * eşlemesi isim üzerinden çalışır.
 */
public enum CustomFieldType {
	TEXT, NUMBER, DATE, BOOLEAN
}
