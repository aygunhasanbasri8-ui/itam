package tr.com.nkolay.itam.controller;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tr.com.nkolay.itam.model.dto.BulkStatusUpdateRequest;
import tr.com.nkolay.itam.model.dto.BulkStatusUpdateResponse;
import tr.com.nkolay.itam.service.asset.AssetBulkService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetBulkController {

    private final AssetBulkService assetBulkService;

    public AssetBulkController(AssetBulkService assetBulkService) {
        this.assetBulkService = assetBulkService;
    }

    @PatchMapping("/bulk-status")
    public BulkStatusUpdateResponse updateStatus(@Valid @RequestBody BulkStatusUpdateRequest request) {
        return assetBulkService.updateStatus(request);
    }
}
