package tr.com.nkolay.itam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import tr.com.nkolay.itam.model.entity.AssetTypeDefinition;

/** Asset tipi tanımlarına erişim. */
public interface AssetTypeDefinitionRepository
		extends JpaRepository<AssetTypeDefinition, String> {

	List<AssetTypeDefinition> findByActiveTrueOrderBySortOrderAsc();

	boolean existsByCodeAndSystemTrue(String code);
}
