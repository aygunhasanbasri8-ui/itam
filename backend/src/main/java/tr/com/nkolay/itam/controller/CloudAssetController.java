package tr.com.nkolay.itam.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.enums.CloudProvider;
import tr.com.nkolay.itam.model.dto.CloudAssetRequest;
import tr.com.nkolay.itam.model.dto.CloudAssetResponse;
import tr.com.nkolay.itam.model.dto.PageResponse;
import tr.com.nkolay.itam.service.asset.CloudAssetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cloud-assets")
public class CloudAssetController {

    private final CloudAssetService cloudAssetService;

    public CloudAssetController(CloudAssetService cloudAssetService) {
        this.cloudAssetService = cloudAssetService;
    }

    @GetMapping
    public PageResponse<CloudAssetResponse> list(
            @RequestParam(required = false) CloudProvider provider,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) AssetStatus status,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return cloudAssetService.list(provider, region, status, pageable);
    }

    @GetMapping("/{id}")
    public CloudAssetResponse get(@PathVariable Long id) {
        return cloudAssetService.get(id);
    }

    @PostMapping
    public ResponseEntity<CloudAssetResponse> create(@Valid @RequestBody CloudAssetRequest request) {
        CloudAssetResponse created = cloudAssetService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public CloudAssetResponse update(@PathVariable Long id, @Valid @RequestBody CloudAssetRequest request) {
        return cloudAssetService.update(id, request);
    }
}
