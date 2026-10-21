package com.itam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.itam.model.entity.AssetTypeDefinition;

/** Asset tipi tanımlarına erişim. */
public interface AssetTypeDefinitionRepository
		extends JpaRepository<AssetTypeDefinition, String> {

	List<AssetTypeDefinition> findByActiveTrueOrderBySortOrderAsc();

	boolean existsByCodeAndSystemTrue(String code);
}
