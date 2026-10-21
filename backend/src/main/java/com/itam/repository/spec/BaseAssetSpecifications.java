package com.itam.repository.spec;

import org.springframework.data.jpa.domain.Specification;

import com.itam.enums.AssetStatus;
import com.itam.model.entity.BaseAsset;

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

    public static <T extends BaseAsset> Specification<T> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static <T extends BaseAsset> Specification<T> nameContains(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String pattern = "%" + text.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("assetName")), pattern);
    }

    public static <T extends BaseAsset> Specification<T> assetTypeEquals(String assetTypeCode) {
        if (assetTypeCode == null || assetTypeCode.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("assetType").get("code"), assetTypeCode);
    }

    public static <T extends BaseAsset> Specification<T> statusEquals(AssetStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
