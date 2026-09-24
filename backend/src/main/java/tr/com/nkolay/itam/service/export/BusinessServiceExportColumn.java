package tr.com.nkolay.itam.service.export;

import java.util.function.Function;

import tr.com.nkolay.itam.model.entity.BusinessServiceAsset;
import tr.com.nkolay.itam.model.entity.Person;

/** ServerExportColumn ile aynı desen — bkz. oradaki açıklama. */
public enum BusinessServiceExportColumn {

    ID("id", "ID", a -> a.getId()),
    ASSET_NAME("assetName", "Ad", BusinessServiceAsset::getAssetName),
    STATUS("status", "Durum", a -> a.getStatus() == null ? null : a.getStatus().name()),
    RESPONSIBILITY_OWNER("responsibilityOwner", "Sorumlu", a -> personLabel(a.getResponsibilityOwner())),
    CRITICALITY("criticality", "Kritiklik", a -> a.getCriticality() == null ? null : a.getCriticality().name()),
    SLA("sla", "SLA", BusinessServiceAsset::getSla),
    SERVICE_OWNER("serviceOwner", "Servis Sorumlusu", a -> personLabel(a.getServiceOwner())),
    NOTES("notes", "Not", BusinessServiceAsset::getNotes),
    CREATED_AT("createdAt", "Oluşturulma", a -> a.getCreatedAt() == null ? null : a.getCreatedAt().toString()),
    UPDATED_AT("updatedAt", "Güncellenme", a -> a.getUpdatedAt() == null ? null : a.getUpdatedAt().toString());

    private final String code;
    private final String header;
    private final Function<BusinessServiceAsset, Object> valueOf;

    BusinessServiceExportColumn(String code, String header, Function<BusinessServiceAsset, Object> valueOf) {
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

    public Object valueOf(BusinessServiceAsset asset) {
        return valueOf.apply(asset);
    }

    public static BusinessServiceExportColumn byCode(String code) {
        for (BusinessServiceExportColumn column : values()) {
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
