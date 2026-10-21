package com.itam.service.asset;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;

import com.itam.enums.AssetStatus;
import com.itam.model.dto.ApplicationAssetRequest;
import com.itam.model.dto.ApplicationAssetResponse;
import com.itam.model.dto.PageResponse;

public interface ApplicationAssetService {

    PageResponse<ApplicationAssetResponse> list(String vendor, AssetStatus status, Long responsibilityOwnerId,
            LocalDate expiryBefore, Pageable pageable);

    ApplicationAssetResponse get(Long id);

    ApplicationAssetResponse create(ApplicationAssetRequest request);

    ApplicationAssetResponse update(Long id, ApplicationAssetRequest request);
}
