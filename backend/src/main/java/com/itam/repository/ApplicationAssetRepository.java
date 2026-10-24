package com.itam.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.itam.model.entity.ApplicationAsset;

public interface ApplicationAssetRepository
        extends JpaRepository<ApplicationAsset, Long>, JpaSpecificationExecutor<ApplicationAsset> {

    /** Dashboard'daki "süresi geçmiş lisanslar" sayacı için. */
    long countByDeletedFalseAndExpiryDateBefore(LocalDate date);

    /** Dashboard'daki "süresi yaklaşan lisanslar" sayacı için; iki sınır da dahil. */
    long countByDeletedFalseAndExpiryDateBetween(LocalDate start, LocalDate end);

    /** Bitiş bildirimleri için; sorumlunun e-postası da gerektiği için birlikte yükleniyor. */
    @EntityGraph(attributePaths = "responsibilityOwner")
    List<ApplicationAsset> findByDeletedFalseAndExpiryDateBetween(LocalDate start, LocalDate end);
}
