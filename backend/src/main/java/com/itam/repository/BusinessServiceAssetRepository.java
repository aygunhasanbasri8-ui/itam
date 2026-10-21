package com.itam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.itam.model.entity.BusinessServiceAsset;

public interface BusinessServiceAssetRepository
        extends JpaRepository<BusinessServiceAsset, Long>, JpaSpecificationExecutor<BusinessServiceAsset> {
}
