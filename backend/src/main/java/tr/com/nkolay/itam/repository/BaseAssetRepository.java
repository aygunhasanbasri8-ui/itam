package tr.com.nkolay.itam.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tr.com.nkolay.itam.model.entity.BaseAsset;

/**
 * Tip fark etmeksizin ortak asset alanlarına (status, is_deleted) erişim için.
 * Toplu durum güncelleme beş asset tipini de kapsıyor; hangi tip olduğuna
 * bakmadan BaseAsset üzerinden çalışmak, her tip için ayrı kod yazmaktan
 * kurtarıyor (JOINED kalıtım sayesinde tek sorguyla hepsi gelir).
 */
public interface BaseAssetRepository extends JpaRepository<BaseAsset, Long> {
}
