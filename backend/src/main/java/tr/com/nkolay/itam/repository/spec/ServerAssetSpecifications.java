package tr.com.nkolay.itam.repository.spec;

import org.springframework.data.jpa.domain.Specification;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.enums.ServerEnvironment;
import tr.com.nkolay.itam.model.entity.ServerAsset;

/**
 * Sunucu liste ekranındaki her filtre kriteri burada ayrı bir Specification.
 * Kriter boşsa null döner; Specification.where(...).and(...) null'ı yok
 * sayar, böylece servis katmanında "bu filtre girildi mi" kontrolünü tekrar
 * yazmaya gerek kalmaz.
 */
public final class ServerAssetSpecifications {

    private ServerAssetSpecifications() {
    }

    public static Specification<ServerAsset> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<ServerAsset> hostnameContains(String hostname) {
        if (hostname == null || hostname.isBlank()) {
            return null;
        }
        String pattern = "%" + hostname.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("hostname")), pattern);
    }

    public static Specification<ServerAsset> environmentEquals(ServerEnvironment environment) {
        if (environment == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("environment"), environment);
    }

    public static Specification<ServerAsset> statusEquals(AssetStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
