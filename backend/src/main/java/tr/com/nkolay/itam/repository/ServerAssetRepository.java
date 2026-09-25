package tr.com.nkolay.itam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import tr.com.nkolay.itam.model.entity.ServerAsset;

/**
 * JpaSpecificationExecutor sayesinde liste ekranındaki filtreler sabit
 * query metotları yazmadan, repository.spec paketindeki Specification'lar
 * birleştirilerek kurulur.
 */
public interface ServerAssetRepository
        extends JpaRepository<ServerAsset, Long>, JpaSpecificationExecutor<ServerAsset> {

    /** Dashboard özet ekranındaki ortam dağılımı için; her satır [ServerEnvironment, count]. */
    @Query("select s.environment, count(s) from ServerAsset s where s.deleted = false group by s.environment")
    List<Object[]> countByEnvironmentGrouped();
}
