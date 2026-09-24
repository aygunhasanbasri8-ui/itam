package tr.com.nkolay.itam.controller;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
import tr.com.nkolay.itam.model.dto.ApplicationAssetRequest;
import tr.com.nkolay.itam.model.dto.ApplicationAssetResponse;
import tr.com.nkolay.itam.model.dto.PageResponse;
import tr.com.nkolay.itam.service.asset.ApplicationAssetService;
import tr.com.nkolay.itam.service.export.ApplicationExportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationAssetController {

    private static final MediaType XLSX_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ApplicationAssetService applicationAssetService;
    private final ApplicationExportService applicationExportService;

    public ApplicationAssetController(ApplicationAssetService applicationAssetService,
            ApplicationExportService applicationExportService) {
        this.applicationAssetService = applicationAssetService;
        this.applicationExportService = applicationExportService;
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

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String vendor,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryBefore,
            @RequestParam(required = false) List<String> columns,
            Authentication authentication) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        applicationExportService.writeXlsx(out, vendor, status, expiryBefore, columns, authentication.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=uygulama-yazilim.xlsx")
                .contentType(XLSX_MEDIA_TYPE)
                .body(out.toByteArray());
    }
}
