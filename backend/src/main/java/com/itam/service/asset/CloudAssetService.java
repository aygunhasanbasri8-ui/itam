package com.itam.service.asset;

import org.springframework.data.domain.Pageable;

import com.itam.enums.AssetStatus;
import com.itam.enums.CloudProvider;
import com.itam.model.dto.CloudAssetRequest;
import com.itam.model.dto.CloudAssetResponse;
import com.itam.model.dto.PageResponse;

public interface CloudAssetService {

    PageResponse<CloudAssetResponse> list(CloudProvider provider, String region,
            AssetStatus status, Long responsibilityOwnerId, Pageable pageable);

    CloudAssetResponse get(Long id);

    CloudAssetResponse create(CloudAssetRequest request);

    CloudAssetResponse update(Long id, CloudAssetRequest request);
}
