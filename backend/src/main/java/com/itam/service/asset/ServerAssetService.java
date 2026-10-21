package com.itam.service.asset;

import org.springframework.data.domain.Pageable;

import com.itam.enums.AssetStatus;
import com.itam.enums.ServerEnvironment;
import com.itam.model.dto.PageResponse;
import com.itam.model.dto.ServerAssetRequest;
import com.itam.model.dto.ServerAssetResponse;

/** Controller yalnızca bu arayüzü görür; gerçekleştirim service.asset.impl altında. */
public interface ServerAssetService {

    PageResponse<ServerAssetResponse> list(String hostname, ServerEnvironment environment,
            AssetStatus status, Long responsibilityOwnerId, Pageable pageable);

    ServerAssetResponse get(Long id);

    ServerAssetResponse create(ServerAssetRequest request);

    ServerAssetResponse update(Long id, ServerAssetRequest request);
}
