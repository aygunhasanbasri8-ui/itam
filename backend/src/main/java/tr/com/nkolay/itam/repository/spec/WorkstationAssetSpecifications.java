package tr.com.nkolay.itam.repository.spec;

import org.springframework.data.jpa.domain.Specification;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.model.entity.WorkstationAsset;

public final class WorkstationAssetSpecifications {

    private WorkstationAssetSpecifications() {
    }

    public static Specification<WorkstationAsset> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<WorkstationAsset> serialNumberContains(String serialNumber) {
        if (serialNumber == null || serialNumber.isBlank()) {
            return null;
        }
        String pattern = "%" + serialNumber.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("serialNumber")), pattern);
    }

    public static Specification<WorkstationAsset> statusEquals(AssetStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<WorkstationAsset> departmentEquals(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("department").get("id"), departmentId);
    }

    public static Specification<WorkstationAsset> assignedPersonEquals(Long personId) {
        if (personId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("assignedPerson").get("id"), personId);
    }
}
