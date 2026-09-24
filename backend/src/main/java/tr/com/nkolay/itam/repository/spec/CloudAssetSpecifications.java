package tr.com.nkolay.itam.repository.spec;

import org.springframework.data.jpa.domain.Specification;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.enums.CloudProvider;
import tr.com.nkolay.itam.model.entity.CloudAsset;

public final class CloudAssetSpecifications {

    private CloudAssetSpecifications() {
    }

    public static Specification<CloudAsset> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<CloudAsset> providerEquals(CloudProvider provider) {
        if (provider == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("cloudProvider"), provider);
    }

    public static Specification<CloudAsset> regionEquals(String region) {
        if (region == null || region.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("region"), region);
    }

    public static Specification<CloudAsset> statusEquals(AssetStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
