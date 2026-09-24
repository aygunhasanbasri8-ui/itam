package tr.com.nkolay.itam.service.asset;

import org.springframework.data.domain.Pageable;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.enums.CloudProvider;
import tr.com.nkolay.itam.model.dto.CloudAssetRequest;
import tr.com.nkolay.itam.model.dto.CloudAssetResponse;
import tr.com.nkolay.itam.model.dto.PageResponse;

public interface CloudAssetService {

    PageResponse<CloudAssetResponse> list(CloudProvider provider, String region,
            AssetStatus status, Pageable pageable);

    CloudAssetResponse get(Long id);

    CloudAssetResponse create(CloudAssetRequest request);

    CloudAssetResponse update(Long id, CloudAssetRequest request);
}
