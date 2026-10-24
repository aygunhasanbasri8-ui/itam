package com.itam.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.springframework.web.multipart.MultipartFile;

import com.itam.enums.AssetStatus;
import com.itam.enums.CloudProvider;
import com.itam.model.dto.CloudAssetRequest;
import com.itam.model.dto.CloudAssetResponse;
import com.itam.model.dto.ExportColumnResponse;
import com.itam.model.dto.ImportReport;
import com.itam.model.dto.PageResponse;
import com.itam.service.asset.CloudAssetService;
import com.itam.service.export.CloudExportService;
import com.itam.service.importer.AssetImportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cloud-assets")
public class CloudAssetController {

    private static final MediaType XLSX_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final CloudAssetService cloudAssetService;
    private final CloudExportService cloudExportService;
    private final AssetImportService<CloudAssetRequest> importService;

    public CloudAssetController(CloudAssetService cloudAssetService, CloudExportService cloudExportService,
            AssetImportService<CloudAssetRequest> importService) {
        this.cloudAssetService = cloudAssetService;
        this.cloudExportService = cloudExportService;
        this.importService = importService;
    }

    @GetMapping
    public PageResponse<CloudAssetResponse> list(
            @RequestParam(required = false) CloudProvider provider,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) Long responsibilityOwnerId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return cloudAssetService.list(provider, region, status, responsibilityOwnerId, pageable);
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

    @GetMapping("/export/columns")
    public List<ExportColumnResponse> exportColumns() {
        return cloudExportService.columns();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) CloudProvider provider,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) Long responsibilityOwnerId,
            @RequestParam(required = false) List<String> columns,
            Authentication authentication) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cloudExportService.writeXlsx(out, provider, region, status, responsibilityOwnerId, columns,
                authentication.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cloud-varliklari.xlsx")
                .contentType(XLSX_MEDIA_TYPE)
                .body(out.toByteArray());
    }

    /** Sunucu içe aktarmasıyla aynı akış: önce dryRun=true ile kontrol, sonra yazma. */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportReport importXlsx(@RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "true") boolean dryRun) throws IOException {
        try (InputStream in = file.getInputStream()) {
            return importService.importXlsx(in, dryRun);
        }
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> importTemplate() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        importService.writeTemplate(out);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bulut-kaynagi-ice-aktarma-sablonu.xlsx")
                .contentType(XLSX_MEDIA_TYPE)
                .body(out.toByteArray());
    }
}
