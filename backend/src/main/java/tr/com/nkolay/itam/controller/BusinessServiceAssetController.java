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
import tr.com.nkolay.itam.enums.ServiceCriticality;
import tr.com.nkolay.itam.model.dto.BusinessServiceAssetRequest;
import tr.com.nkolay.itam.model.dto.BusinessServiceAssetResponse;
import tr.com.nkolay.itam.model.dto.ExportColumnResponse;
import tr.com.nkolay.itam.model.dto.PageResponse;
import tr.com.nkolay.itam.service.asset.BusinessServiceAssetService;
import tr.com.nkolay.itam.service.export.BusinessServiceExportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/business-services")
public class BusinessServiceAssetController {

    private static final MediaType XLSX_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final BusinessServiceAssetService businessServiceAssetService;
    private final BusinessServiceExportService businessServiceExportService;

    public BusinessServiceAssetController(BusinessServiceAssetService businessServiceAssetService,
            BusinessServiceExportService businessServiceExportService) {
        this.businessServiceAssetService = businessServiceAssetService;
        this.businessServiceExportService = businessServiceExportService;
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

    @GetMapping("/export/columns")
    public List<ExportColumnResponse> exportColumns() {
        return businessServiceExportService.columns();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) ServiceCriticality criticality,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) List<String> columns,
            Authentication authentication) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        businessServiceExportService.writeXlsx(out, criticality, status, columns, authentication.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=is-servisleri.xlsx")
                .contentType(XLSX_MEDIA_TYPE)
                .body(out.toByteArray());
    }
}
