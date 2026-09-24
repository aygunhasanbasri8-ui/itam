package tr.com.nkolay.itam.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
import tr.com.nkolay.itam.model.dto.ApplicationAssetRequest;
import tr.com.nkolay.itam.model.dto.ApplicationAssetResponse;
import tr.com.nkolay.itam.model.dto.PageResponse;
import tr.com.nkolay.itam.service.asset.ApplicationAssetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationAssetController {

    private final ApplicationAssetService applicationAssetService;

    public ApplicationAssetController(ApplicationAssetService applicationAssetService) {
        this.applicationAssetService = applicationAssetService;
    }

    @GetMapping
    public PageResponse<ApplicationAssetResponse> list(
            @RequestParam(required = false) String vendor,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryBefore,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return applicationAssetService.list(vendor, status, expiryBefore, pageable);
    }

    @GetMapping("/{id}")
    public ApplicationAssetResponse get(@PathVariable Long id) {
        return applicationAssetService.get(id);
    }

    @PostMapping
    public ResponseEntity<ApplicationAssetResponse> create(
            @Valid @RequestBody ApplicationAssetRequest request) {
        ApplicationAssetResponse created = applicationAssetService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ApplicationAssetResponse update(@PathVariable Long id,
            @Valid @RequestBody ApplicationAssetRequest request) {
        return applicationAssetService.update(id, request);
    }
}
