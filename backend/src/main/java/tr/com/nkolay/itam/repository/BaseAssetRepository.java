package tr.com.nkolay.itam.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import tr.com.nkolay.itam.model.entity.BaseAsset;

/**
 * Tip fark etmeksizin ortak asset alanlarına (status, is_deleted) erişim için.
 * Toplu durum güncelleme beş asset tipini de kapsıyor; hangi tip olduğuna
 * bakmadan BaseAsset üzerinden çalışmak, her tip için ayrı kod yazmaktan
 * kurtarıyor (JOINED kalıtım sayesinde tek sorguyla hepsi gelir).
 */
public interface BaseAssetRepository extends JpaRepository<BaseAsset, Long> {

    long countByDeletedFalse();

    /** Dashboard özet ekranı için durum bazlı dağılım; her satır [AssetStatus, count]. */
    @Query("select b.status, count(b) from BaseAsset b where b.deleted = false group by b.status")
    List<Object[]> countByStatusGrouped();

    /** Dashboard özet ekranı için asset tipi bazlı dağılım; her satır [assetTypeCode, count]. */
    @Query("select b.assetType.code, count(b) from BaseAsset b where b.deleted = false group by b.assetType.code")
    List<Object[]> countByAssetTypeGrouped();

    @EntityGraph(attributePaths = "assetType")
    List<BaseAsset> findByDeletedFalseAndAssetNameContainingIgnoreCaseOrderByAssetName(String assetName,
            Pageable pageable);
}
