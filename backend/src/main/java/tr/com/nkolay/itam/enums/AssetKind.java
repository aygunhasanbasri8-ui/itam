package tr.com.nkolay.itam.enums;

/**
 * JPA kalıtım ayırt edici değeri. Hangi entity sınıfının kullanılacağını belirler.
 *
 * base_asset.asset_type kolonundan farklıdır: o dinamiktir (yönetici arayüzden
 * yeni tip ekleyebilir), bu kapalı bir kümedir. GENERIC = yönetici tanımlı tip,
 * detay tablosu yok.
 * <p>
 * Değer adları PostgreSQL enum tipiyle birebir aynıdır; Hibernate NAMED_ENUM
 * eşlemesi isim üzerinden çalışır.
 */
public enum AssetKind {
	SERVER, WORKSTATION, CLOUD, APPLICATION, BUSINESS_SERVICE, GENERIC
}
