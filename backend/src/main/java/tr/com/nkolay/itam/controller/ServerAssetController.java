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
import tr.com.nkolay.itam.enums.ServerEnvironment;
import tr.com.nkolay.itam.model.dto.PageResponse;
import tr.com.nkolay.itam.model.dto.ServerAssetRequest;
import tr.com.nkolay.itam.model.dto.ServerAssetResponse;
import tr.com.nkolay.itam.service.asset.ServerAssetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/servers")
public class ServerAssetController {

    private final ServerAssetService serverAssetService;

    public ServerAssetController(ServerAssetService serverAssetService) {
        this.serverAssetService = serverAssetService;
    }

    @GetMapping
    public PageResponse<ServerAssetResponse> list(
            @RequestParam(required = false) String hostname,
            @RequestParam(required = false) ServerEnvironment environment,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) Long responsibilityOwnerId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return serverAssetService.list(hostname, environment, status, responsibilityOwnerId, pageable);
    }

    @GetMapping("/{id}")
    public ServerAssetResponse get(@PathVariable Long id) {
        return serverAssetService.get(id);
    }

    @PostMapping
    public ResponseEntity<ServerAssetResponse> create(@Valid @RequestBody ServerAssetRequest request) {
        ServerAssetResponse created = serverAssetService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ServerAssetResponse update(@PathVariable Long id, @Valid @RequestBody ServerAssetRequest request) {
        return serverAssetService.update(id, request);
    }
}
