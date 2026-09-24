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
import tr.com.nkolay.itam.model.dto.PageResponse;
import tr.com.nkolay.itam.model.dto.WorkstationAssetRequest;
import tr.com.nkolay.itam.model.dto.WorkstationAssetResponse;
import tr.com.nkolay.itam.service.asset.WorkstationAssetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/workstations")
public class WorkstationAssetController {

    private final WorkstationAssetService workstationAssetService;

    public WorkstationAssetController(WorkstationAssetService workstationAssetService) {
        this.workstationAssetService = workstationAssetService;
    }

    @GetMapping
    public PageResponse<WorkstationAssetResponse> list(
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long assignedPersonId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return workstationAssetService.list(serialNumber, status, departmentId, assignedPersonId, pageable);
    }

    @GetMapping("/{id}")
    public WorkstationAssetResponse get(@PathVariable Long id) {
        return workstationAssetService.get(id);
    }

    @PostMapping
    public ResponseEntity<WorkstationAssetResponse> create(@Valid @RequestBody WorkstationAssetRequest request) {
        WorkstationAssetResponse created = workstationAssetService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public WorkstationAssetResponse update(@PathVariable Long id,
            @Valid @RequestBody WorkstationAssetRequest request) {
        return workstationAssetService.update(id, request);
    }
}
