package tr.com.nkolay.itam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import tr.com.nkolay.itam.model.entity.ServerAsset;

/**
 * JpaSpecificationExecutor sayesinde liste ekranındaki filtreler sabit
 * query metotları yazmadan, repository.spec paketindeki Specification'lar
 * birleştirilerek kurulur.
 */
public interface ServerAssetRepository
        extends JpaRepository<ServerAsset, Long>, JpaSpecificationExecutor<ServerAsset> {
}
