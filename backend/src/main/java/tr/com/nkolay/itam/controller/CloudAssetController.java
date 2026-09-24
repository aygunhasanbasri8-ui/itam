package tr.com.nkolay.itam.controller;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import tr.com.nkolay.itam.service.export.CloudExportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cloud-assets")
public class CloudAssetController {

    private static final MediaType XLSX_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final CloudAssetService cloudAssetService;
    private final CloudExportService cloudExportService;

    public CloudAssetController(CloudAssetService cloudAssetService, CloudExportService cloudExportService) {
        this.cloudAssetService = cloudAssetService;
        this.cloudExportService = cloudExportService;
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

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) CloudProvider provider,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) List<String> columns,
            Authentication authentication) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cloudExportService.writeXlsx(out, provider, region, status, columns, authentication.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cloud-varliklari.xlsx")
                .contentType(XLSX_MEDIA_TYPE)
                .body(out.toByteArray());
    }
}
