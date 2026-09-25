package tr.com.nkolay.itam.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import tr.com.nkolay.itam.model.entity.ApplicationAsset;

public interface ApplicationAssetRepository
        extends JpaRepository<ApplicationAsset, Long>, JpaSpecificationExecutor<ApplicationAsset> {

    /** Dashboard'daki "süresi geçmiş lisanslar" sayacı için. */
    long countByDeletedFalseAndExpiryDateBefore(LocalDate date);

    /** Dashboard'daki "süresi yaklaşan lisanslar" sayacı için; iki sınır da dahil. */
    long countByDeletedFalseAndExpiryDateBetween(LocalDate start, LocalDate end);
}
