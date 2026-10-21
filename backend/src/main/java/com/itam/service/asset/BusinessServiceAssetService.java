package com.itam.service.asset;

import org.springframework.data.domain.Pageable;

import com.itam.enums.AssetStatus;
import com.itam.enums.ServiceCriticality;
import com.itam.model.dto.BusinessServiceAssetRequest;
import com.itam.model.dto.BusinessServiceAssetResponse;
import com.itam.model.dto.PageResponse;

public interface BusinessServiceAssetService {

    PageResponse<BusinessServiceAssetResponse> list(ServiceCriticality criticality,
            AssetStatus status, Long responsibilityOwnerId, Pageable pageable);

    BusinessServiceAssetResponse get(Long id);

    BusinessServiceAssetResponse create(BusinessServiceAssetRequest request);

    BusinessServiceAssetResponse update(Long id, BusinessServiceAssetRequest request);
}
