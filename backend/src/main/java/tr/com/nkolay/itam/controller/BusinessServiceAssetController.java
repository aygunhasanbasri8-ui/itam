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
import tr.com.nkolay.itam.enums.ServiceCriticality;
import tr.com.nkolay.itam.model.dto.BusinessServiceAssetRequest;
import tr.com.nkolay.itam.model.dto.BusinessServiceAssetResponse;
import tr.com.nkolay.itam.model.dto.PageResponse;
import tr.com.nkolay.itam.service.asset.BusinessServiceAssetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/business-services")
public class BusinessServiceAssetController {

    private final BusinessServiceAssetService businessServiceAssetService;

    public BusinessServiceAssetController(BusinessServiceAssetService businessServiceAssetService) {
        this.businessServiceAssetService = businessServiceAssetService;
    }

    @GetMapping
    public PageResponse<BusinessServiceAssetResponse> list(
            @RequestParam(required = false) ServiceCriticality criticality,
            @RequestParam(required = false) AssetStatus status,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return businessServiceAssetService.list(criticality, status, pageable);
    }

    @GetMapping("/{id}")
    public BusinessServiceAssetResponse get(@PathVariable Long id) {
        return businessServiceAssetService.get(id);
    }

    @PostMapping
    public ResponseEntity<BusinessServiceAssetResponse> create(
            @Valid @RequestBody BusinessServiceAssetRequest request) {
        BusinessServiceAssetResponse created = businessServiceAssetService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public BusinessServiceAssetResponse update(@PathVariable Long id,
            @Valid @RequestBody BusinessServiceAssetRequest request) {
        return businessServiceAssetService.update(id, request);
    }
}
