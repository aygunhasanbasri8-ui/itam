package com.itam.service.export;

import java.util.function.Function;

import com.itam.model.entity.CloudAsset;
import com.itam.model.entity.Person;

/** ServerExportColumn ile aynı desen — bkz. oradaki açıklama. */
public enum CloudExportColumn implements AssetColumn<CloudAsset> {

    ID("id", "ID", a -> a.getId()),
    ASSET_NAME("assetName", "Ad", CloudAsset::getAssetName),
    STATUS("status", "Durum", a -> a.getStatus() == null ? null : a.getStatus().name()),
    RESPONSIBILITY_OWNER("responsibilityOwner", "Sorumlu", a -> personLabel(a.getResponsibilityOwner())),
    CLOUD_PROVIDER("cloudProvider", "Sağlayıcı", a -> a.getCloudProvider() == null ? null : a.getCloudProvider().name()),
    INSTANCE_TYPE("instanceType", "Instance Tipi", CloudAsset::getInstanceType),
    REGION("region", "Bölge", CloudAsset::getRegion),
    ACCOUNT_ID("accountId", "Hesap ID", CloudAsset::getAccountId),
    NOTES("notes", "Not", CloudAsset::getNotes),
    CREATED_AT("createdAt", "Oluşturulma", a -> a.getCreatedAt() == null ? null : a.getCreatedAt().toString()),
    UPDATED_AT("updatedAt", "Güncellenme", a -> a.getUpdatedAt() == null ? null : a.getUpdatedAt().toString());

    private final String code;
    private final String header;
    private final Function<CloudAsset, Object> valueOf;

    CloudExportColumn(String code, String header, Function<CloudAsset, Object> valueOf) {
        this.code = code;
        this.header = header;
        this.valueOf = valueOf;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String header() {
        return header;
    }

    @Override
    public Object valueOf(CloudAsset asset) {
        return valueOf.apply(asset);
    }

    public static CloudExportColumn byCode(String code) {
        for (CloudExportColumn column : values()) {
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
