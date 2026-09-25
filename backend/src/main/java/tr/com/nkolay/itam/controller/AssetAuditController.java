package tr.com.nkolay.itam.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import tr.com.nkolay.itam.model.dto.AssetRevisionResponse;
import tr.com.nkolay.itam.service.audit.AssetAuditService;

@RestController
public class AssetAuditController {

    private final AssetAuditService assetAuditService;

    public AssetAuditController(AssetAuditService assetAuditService) {
        this.assetAuditService = assetAuditService;
    }

    @GetMapping("/api/v1/assets/{assetId}/audit-trail")
    public List<AssetRevisionResponse> auditTrail(@PathVariable Long assetId) {
        return assetAuditService.getHistory(assetId);
    }
}
