package com.itam.repository.spec;

import org.springframework.data.jpa.domain.Specification;

import com.itam.enums.AssetStatus;
import com.itam.enums.ServiceCriticality;
import com.itam.model.entity.BusinessServiceAsset;

public final class BusinessServiceAssetSpecifications {

    private BusinessServiceAssetSpecifications() {
    }

    public static Specification<BusinessServiceAsset> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<BusinessServiceAsset> criticalityEquals(ServiceCriticality criticality) {
        if (criticality == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("criticality"), criticality);
    }

    public static Specification<BusinessServiceAsset> statusEquals(AssetStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
