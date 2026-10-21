package com.itam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.itam.model.entity.CloudAsset;

public interface CloudAssetRepository
        extends JpaRepository<CloudAsset, Long>, JpaSpecificationExecutor<CloudAsset> {
}
