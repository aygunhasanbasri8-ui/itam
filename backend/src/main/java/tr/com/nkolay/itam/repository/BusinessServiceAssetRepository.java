package tr.com.nkolay.itam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import tr.com.nkolay.itam.model.entity.BusinessServiceAsset;

public interface BusinessServiceAssetRepository
        extends JpaRepository<BusinessServiceAsset, Long>, JpaSpecificationExecutor<BusinessServiceAsset> {
}
