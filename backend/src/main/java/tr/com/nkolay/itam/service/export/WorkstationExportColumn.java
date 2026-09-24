package tr.com.nkolay.itam.service.export;

import java.util.function.Function;

import tr.com.nkolay.itam.model.entity.Department;
import tr.com.nkolay.itam.model.entity.Person;
import tr.com.nkolay.itam.model.entity.WorkstationAsset;

/** ServerExportColumn ile aynı desen — bkz. oradaki açıklama. */
public enum WorkstationExportColumn {

    ID("id", "ID", a -> a.getId()),
    ASSET_NAME("assetName", "Ad", WorkstationAsset::getAssetName),
    STATUS("status", "Durum", a -> a.getStatus() == null ? null : a.getStatus().name()),
    RESPONSIBILITY_OWNER("responsibilityOwner", "Sorumlu", a -> personLabel(a.getResponsibilityOwner())),
    SERIAL_NUMBER("serialNumber", "Seri No", WorkstationAsset::getSerialNumber),
    MODEL("model", "Model", WorkstationAsset::getModel),
    CPU("cpu", "İşlemci", WorkstationAsset::getCpu),
    RAM_GB("ramGb", "RAM (GB)", WorkstationAsset::getRamGb),
    DISK_GB("diskGb", "Disk (GB)", WorkstationAsset::getDiskGb),
    ASSIGNED_PERSON("assignedPerson", "Zimmetli Kişi", a -> personLabel(a.getAssignedPerson())),
    DEPARTMENT("department", "Departman", a -> departmentLabel(a.getDepartment())),
    NOTES("notes", "Not", WorkstationAsset::getNotes),
    CREATED_AT("createdAt", "Oluşturulma", a -> a.getCreatedAt() == null ? null : a.getCreatedAt().toString()),
    UPDATED_AT("updatedAt", "Güncellenme", a -> a.getUpdatedAt() == null ? null : a.getUpdatedAt().toString());

    private final String code;
    private final String header;
    private final Function<WorkstationAsset, Object> valueOf;

    WorkstationExportColumn(String code, String header, Function<WorkstationAsset, Object> valueOf) {
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

    public Object valueOf(WorkstationAsset asset) {
        return valueOf.apply(asset);
    }

    public static WorkstationExportColumn byCode(String code) {
        for (WorkstationExportColumn column : values()) {
            if (column.code.equalsIgnoreCase(code)) {
                return column;
            }
        }
        throw new IllegalArgumentException("Bilinmeyen export kolonu: " + code);
    }

    private static String personLabel(Person person) {
        return person == null ? null : person.getFullName();
    }

    private static String departmentLabel(Department department) {
        return department == null ? null : department.getName();
    }
}
