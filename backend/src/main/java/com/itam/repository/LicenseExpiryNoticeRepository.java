package com.itam.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.itam.model.entity.LicenseExpiryNotice;

public interface LicenseExpiryNoticeRepository extends JpaRepository<LicenseExpiryNotice, Long> {

    List<LicenseExpiryNotice> findByAssetIdAndExpiryDate(Long assetId, LocalDate expiryDate);
}
