package tr.com.nkolay.itam.service.asset;

import org.springframework.data.domain.Pageable;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.enums.ServerEnvironment;
import tr.com.nkolay.itam.model.dto.PageResponse;
import tr.com.nkolay.itam.model.dto.ServerAssetRequest;
import tr.com.nkolay.itam.model.dto.ServerAssetResponse;

/** Controller yalnızca bu arayüzü görür; gerçekleştirim service.asset.impl altında. */
public interface ServerAssetService {

    PageResponse<ServerAssetResponse> list(String hostname, ServerEnvironment environment,
            AssetStatus status, Long responsibilityOwnerId, Pageable pageable);

    ServerAssetResponse get(Long id);

    ServerAssetResponse create(ServerAssetRequest request);

    ServerAssetResponse update(Long id, ServerAssetRequest request);
}
