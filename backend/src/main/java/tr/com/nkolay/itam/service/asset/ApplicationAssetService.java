package tr.com.nkolay.itam.service.asset;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.model.dto.ApplicationAssetRequest;
import tr.com.nkolay.itam.model.dto.ApplicationAssetResponse;
import tr.com.nkolay.itam.model.dto.PageResponse;

public interface ApplicationAssetService {

    PageResponse<ApplicationAssetResponse> list(String vendor, AssetStatus status, Long responsibilityOwnerId,
            LocalDate expiryBefore, Pageable pageable);

    ApplicationAssetResponse get(Long id);

    ApplicationAssetResponse create(ApplicationAssetRequest request);

    ApplicationAssetResponse update(Long id, ApplicationAssetRequest request);
}
