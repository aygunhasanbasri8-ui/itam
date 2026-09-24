package tr.com.nkolay.itam.service.asset;

import org.springframework.data.domain.Pageable;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.model.dto.PageResponse;
import tr.com.nkolay.itam.model.dto.WorkstationAssetRequest;
import tr.com.nkolay.itam.model.dto.WorkstationAssetResponse;

public interface WorkstationAssetService {

    PageResponse<WorkstationAssetResponse> list(String serialNumber, AssetStatus status,
            Long departmentId, Long assignedPersonId, Pageable pageable);

    WorkstationAssetResponse get(Long id);

    WorkstationAssetResponse create(WorkstationAssetRequest request);

    WorkstationAssetResponse update(Long id, WorkstationAssetRequest request);
}
