package tr.com.nkolay.itam.repository.spec;

import org.springframework.data.jpa.domain.Specification;

import tr.com.nkolay.itam.model.entity.BaseAsset;

/** Beş asset tipinin ortak alanları üzerinden filtreler; tip başına ayrı ayrı yazmamak için generic. */
public final class BaseAssetSpecifications {

    private BaseAssetSpecifications() {
    }

    public static <T extends BaseAsset> Specification<T> responsibilityOwnerEquals(Long personId) {
        if (personId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("responsibilityOwner").get("id"), personId);
    }
}
