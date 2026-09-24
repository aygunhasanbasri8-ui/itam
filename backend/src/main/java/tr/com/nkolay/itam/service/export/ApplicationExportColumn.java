package tr.com.nkolay.itam.service.export;

import java.util.function.Function;

import tr.com.nkolay.itam.model.entity.ApplicationAsset;
import tr.com.nkolay.itam.model.entity.Person;

/** ServerExportColumn ile aynı desen — bkz. oradaki açıklama. */
public enum ApplicationExportColumn {

    ID("id", "ID", a -> a.getId()),
    ASSET_NAME("assetName", "Ad", ApplicationAsset::getAssetName),
    STATUS("status", "Durum", a -> a.getStatus() == null ? null : a.getStatus().name()),
    RESPONSIBILITY_OWNER("responsibilityOwner", "Sorumlu", a -> personLabel(a.getResponsibilityOwner())),
    VERSION("version", "Sürüm", ApplicationAsset::getVersion),
    LICENSE_TYPE("licenseType", "Lisans Tipi", ApplicationAsset::getLicenseType),
    LICENSE_COUNT("licenseCount", "Lisans Adedi", ApplicationAsset::getLicenseCount),
    VENDOR("vendor", "Tedarikçi", ApplicationAsset::getVendor),
    EXPIRY_DATE("expiryDate", "Bitiş Tarihi", a -> a.getExpiryDate() == null ? null : a.getExpiryDate().toString()),
    INSTALL_PATH("installPath", "Kurulum Yolu", ApplicationAsset::getInstallPath),
    NOTES("notes", "Not", ApplicationAsset::getNotes),
    CREATED_AT("createdAt", "Oluşturulma", a -> a.getCreatedAt() == null ? null : a.getCreatedAt().toString()),
    UPDATED_AT("updatedAt", "Güncellenme", a -> a.getUpdatedAt() == null ? null : a.getUpdatedAt().toString());

    private final String code;
    private final String header;
    private final Function<ApplicationAsset, Object> valueOf;

    ApplicationExportColumn(String code, String header, Function<ApplicationAsset, Object> valueOf) {
        this.code = code;
        this.header = header;
        this.valueOf = valueOf;
    }

    public String code() {
        return code;
    }

    public String header() {
        return header;
    }

    public Object valueOf(ApplicationAsset asset) {
        return valueOf.apply(asset);
    }

    public static ApplicationExportColumn byCode(String code) {
        for (ApplicationExportColumn column : values()) {
            if (column.code.equalsIgnoreCase(code)) {
                return column;
            }
        }
        throw new IllegalArgumentException("Bilinmeyen export kolonu: " + code);
    }

    private static String personLabel(Person person) {
        return person == null ? null : person.getFullName();
    }
}
