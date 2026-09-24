package tr.com.nkolay.itam.service.export;

import java.util.function.Function;

import tr.com.nkolay.itam.model.entity.Person;
import tr.com.nkolay.itam.model.entity.ServerAsset;

/**
 * Excel'e yazılabilecek her kolonun kodu, başlığı ve değerini nasıl okuyacağı
 * burada bir arada. Kullanıcı export isterken kodları seçer (ör. "hostname,
 * environment,os"); hiç seçmezse hepsi yazılır. Yeni bir sunucu alanı
 * eklendiğinde sadece burada yeni bir sabit tanımlamak yeterli.
 */
public enum ServerExportColumn {

    ID("id", "ID", a -> a.getId()),
    ASSET_NAME("assetName", "Ad", ServerAsset::getAssetName),
    STATUS("status", "Durum", a -> a.getStatus() == null ? null : a.getStatus().name()),
    RESPONSIBILITY_OWNER("responsibilityOwner", "Sorumlu", a -> personLabel(a.getResponsibilityOwner())),
    HOSTNAME("hostname", "Hostname", ServerAsset::getHostname),
    DOMAIN("domain", "Domain", ServerAsset::getDomain),
    IP_ADDRESS("ipAddress", "IP Adresi", ServerAsset::getIpAddress),
    ENVIRONMENT("environment", "Ortam", a -> a.getEnvironment() == null ? null : a.getEnvironment().name()),
    SERVER_CLASS("serverClass", "Sunucu Sınıfı", ServerAsset::getServerClass),
    GROUP_NAME("groupName", "Grup", ServerAsset::getGroupName),
    INFRASTRUCTURE_TYPE("infrastructureType", "Altyapı Tipi", ServerAsset::getInfrastructureType),
    OS("os", "İşletim Sistemi", ServerAsset::getOs),
    VIRTUAL("virtual", "Sanal mı", a -> a.isVirtual() ? "Evet" : "Hayır"),
    SERVICE_OWNER("serviceOwner", "Servis Sorumlusu", a -> personLabel(a.getServiceOwner())),
    APP_RESPONSIBLE("appResponsible", "Uygulama Sorumlusu", a -> personLabel(a.getAppResponsible())),
    OS_RESPONSIBLE("osResponsible", "OS Sorumlusu", a -> personLabel(a.getOsResponsible())),
    NOTES("notes", "Not", ServerAsset::getNotes),
    CREATED_AT("createdAt", "Oluşturulma", a -> a.getCreatedAt() == null ? null : a.getCreatedAt().toString()),
    UPDATED_AT("updatedAt", "Güncellenme", a -> a.getUpdatedAt() == null ? null : a.getUpdatedAt().toString());

    private final String code;
    private final String header;
    private final Function<ServerAsset, Object> valueOf;

    ServerExportColumn(String code, String header, Function<ServerAsset, Object> valueOf) {
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

    public Object valueOf(ServerAsset asset) {
        return valueOf.apply(asset);
    }

    public static ServerExportColumn byCode(String code) {
        for (ServerExportColumn column : values()) {
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
