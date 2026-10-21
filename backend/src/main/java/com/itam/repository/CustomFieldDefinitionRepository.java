package com.itam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.itam.model.entity.CustomFieldDefinition;

public interface CustomFieldDefinitionRepository extends JpaRepository<CustomFieldDefinition, Long> {

    List<CustomFieldDefinition> findByAssetTypeCodeOrderBySortOrder(String assetTypeCode);

    boolean existsByAssetTypeCodeAndFieldKey(String assetTypeCode, String fieldKey);
}
