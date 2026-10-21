package tr.com.nkolay.itam.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.model.dto.AssetSummaryResponse;
import tr.com.nkolay.itam.model.dto.PageResponse;
import tr.com.nkolay.itam.service.asset.AssetSearchService;

@RestController
public class AssetSearchController {

    private final AssetSearchService assetSearchService;

    public AssetSearchController(AssetSearchService assetSearchService) {
        this.assetSearchService = assetSearchService;
    }

    @GetMapping("/api/v1/assets/search")
    public List<AssetSummaryResponse> search(@RequestParam(required = false) String q,
            @RequestParam(defaultValue = "20") int limit) {
        return assetSearchService.search(q, limit);
    }

    @GetMapping("/api/v1/assets")
    public PageResponse<AssetSummaryResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String assetType,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) Long responsibilityOwnerId,
            @PageableDefault(size = 20, sort = "assetName") Pageable pageable) {
        return assetSearchService.list(q, assetType, status, responsibilityOwnerId, pageable);
    }
}
