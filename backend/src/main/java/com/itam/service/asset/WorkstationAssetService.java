package com.itam.service.asset;

import org.springframework.data.domain.Pageable;

import com.itam.enums.AssetStatus;
import com.itam.model.dto.PageResponse;
import com.itam.model.dto.WorkstationAssetRequest;
import com.itam.model.dto.WorkstationAssetResponse;

public interface WorkstationAssetService {

    PageResponse<WorkstationAssetResponse> list(String serialNumber, AssetStatus status,
            Long responsibilityOwnerId,
            Long departmentId, Long assignedPersonId, Pageable pageable);

    WorkstationAssetResponse get(Long id);

    WorkstationAssetResponse create(WorkstationAssetRequest request);

    WorkstationAssetResponse update(Long id, WorkstationAssetRequest request);
}
