package tr.com.nkolay.itam.service.asset;

import org.springframework.data.domain.Pageable;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.enums.ServiceCriticality;
import tr.com.nkolay.itam.model.dto.BusinessServiceAssetRequest;
import tr.com.nkolay.itam.model.dto.BusinessServiceAssetResponse;
import tr.com.nkolay.itam.model.dto.PageResponse;

public interface BusinessServiceAssetService {

    PageResponse<BusinessServiceAssetResponse> list(ServiceCriticality criticality,
            AssetStatus status, Long responsibilityOwnerId, Pageable pageable);

    BusinessServiceAssetResponse get(Long id);

    BusinessServiceAssetResponse create(BusinessServiceAssetRequest request);

    BusinessServiceAssetResponse update(Long id, BusinessServiceAssetRequest request);
}
