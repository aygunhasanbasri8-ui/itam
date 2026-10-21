package com.itam.repository.spec;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.itam.enums.AssetStatus;
import com.itam.model.entity.ApplicationAsset;

public final class ApplicationAssetSpecifications {

    private ApplicationAssetSpecifications() {
    }

    public static Specification<ApplicationAsset> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<ApplicationAsset> vendorContains(String vendor) {
        if (vendor == null || vendor.isBlank()) {
            return null;
        }
        String pattern = "%" + vendor.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("vendor")), pattern);
    }

    public static Specification<ApplicationAsset> statusEquals(AssetStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /** Lisansı bu tarihten önce bitecek olanlar — dashboard'daki "süresi yaklaşan lisanslar" için. */
    public static Specification<ApplicationAsset> expiryBefore(LocalDate date) {
        if (date == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThan(root.get("expiryDate"), date);
    }
}
