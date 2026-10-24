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
import com.itam.model.dto.ExportColumnResponse;
import com.itam.model.dto.ImportReport;
import com.itam.model.dto.PageResponse;
import com.itam.model.dto.WorkstationAssetRequest;
import com.itam.model.dto.WorkstationAssetResponse;
import com.itam.service.asset.WorkstationAssetService;
import com.itam.service.export.WorkstationExportService;
import com.itam.service.importer.AssetImportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/workstations")
public class WorkstationAssetController {

    private static final MediaType XLSX_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final WorkstationAssetService workstationAssetService;
    private final WorkstationExportService workstationExportService;
    private final AssetImportService<WorkstationAssetRequest> importService;

    public WorkstationAssetController(WorkstationAssetService workstationAssetService,
            WorkstationExportService workstationExportService,
            AssetImportService<WorkstationAssetRequest> importService) {
        this.workstationAssetService = workstationAssetService;
        this.workstationExportService = workstationExportService;
        this.importService = importService;
    }

    @GetMapping
    public PageResponse<WorkstationAssetResponse> list(
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) Long responsibilityOwnerId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long assignedPersonId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return workstationAssetService.list(serialNumber, status, responsibilityOwnerId, departmentId,
                assignedPersonId, pageable);
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

    @GetMapping("/export/columns")
    public List<ExportColumnResponse> exportColumns() {
        return workstationExportService.columns();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) Long responsibilityOwnerId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long assignedPersonId,
            @RequestParam(required = false) List<String> columns,
            Authentication authentication) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workstationExportService.writeXlsx(out, serialNumber, status, responsibilityOwnerId,
                departmentId, assignedPersonId,
                columns, authentication.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=is-istasyonlari.xlsx")
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
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=is-istasyonu-ice-aktarma-sablonu.xlsx")
                .contentType(XLSX_MEDIA_TYPE)
                .body(out.toByteArray());
    }
}
