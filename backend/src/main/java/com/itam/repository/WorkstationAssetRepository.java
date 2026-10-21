package com.itam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.itam.model.entity.WorkstationAsset;

public interface WorkstationAssetRepository
        extends JpaRepository<WorkstationAsset, Long>, JpaSpecificationExecutor<WorkstationAsset> {
}
