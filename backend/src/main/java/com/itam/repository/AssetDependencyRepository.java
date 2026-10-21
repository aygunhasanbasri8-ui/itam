package com.itam.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.itam.model.entity.AssetDependency;

public interface AssetDependencyRepository extends JpaRepository<AssetDependency, Long> {

    boolean existsByParentAssetIdAndChildAssetId(Long parentAssetId, Long childAssetId);

    boolean existsByParentAssetIdOrChildAssetId(Long parentAssetId, Long childAssetId);

    /** Bir asset'in doğrudan bağımlı olduğu veya ona bağımlı olan kenarlar; isimler tek sorguda gelir. */
    @Query("select d from AssetDependency d "
            + "join fetch d.parentAsset join fetch d.childAsset "
            + "where d.parentAsset.id = :assetId or d.childAsset.id = :assetId")
    List<AssetDependency> findEdgesTouchingAsset(@Param("assetId") Long assetId);

    List<AssetDependency> findByParentAssetIdIn(Collection<Long> parentAssetIds);
}
