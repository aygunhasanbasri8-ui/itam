package com.itam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.itam.model.entity.WorkstationAsset;

public interface WorkstationAssetRepository
        extends JpaRepository<WorkstationAsset, Long>, JpaSpecificationExecutor<WorkstationAsset> {

    /** İçe aktarmada çakışma kontrolü; silinmiş kayıtların seri numarası yeniden kullanılabiliyor (V10). */
    @Query("select upper(w.serialNumber) from WorkstationAsset w where w.deleted = false")
    List<String> findActiveSerialNumbersUpperCase();
}
