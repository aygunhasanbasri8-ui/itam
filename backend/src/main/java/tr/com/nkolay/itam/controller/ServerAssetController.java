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
import tr.com.nkolay.itam.enums.ServerEnvironment;
import tr.com.nkolay.itam.model.dto.PageResponse;
import tr.com.nkolay.itam.model.dto.ServerAssetRequest;
import tr.com.nkolay.itam.model.dto.ServerAssetResponse;
import tr.com.nkolay.itam.service.asset.ServerAssetService;
import tr.com.nkolay.itam.service.export.ServerExportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/servers")
public class ServerAssetController {

    private static final MediaType XLSX_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ServerAssetService serverAssetService;
    private final ServerExportService serverExportService;

    public ServerAssetController(ServerAssetService serverAssetService,
            ServerExportService serverExportService) {
        this.serverAssetService = serverAssetService;
        this.serverExportService = serverExportService;
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

    /**
     * Ekrandaki filtrelerle birebir aynı parametreler kabul edilir ki liste
     * ekranında uygulanan filtre dışa aktarıma da yansısın (tasarım raporu 6.5).
     * <p>
     * SXSSF'in bellek avantajı (satırların pencere halinde tutulması) dosya
     * üretim aşamasında zaten geçerli; burada sonucu StreamingResponseBody ile
     * servlet çıktısına vermek yerine byte[] olarak döndürmek bilinçli bir
     * tercih — MockMvc'nin async dispatch'i kendiliğinden takip etmemesi bir
     * yana, üretilen dosyalar (birkaç bin satır) zaten küçük, senkron yanıt
     * hem test etmesi hem izlemesi daha basit.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String hostname,
            @RequestParam(required = false) ServerEnvironment environment,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) Long responsibilityOwnerId,
            @RequestParam(required = false) List<String> columns,
            Authentication authentication) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serverExportService.writeXlsx(out, hostname, environment, status, responsibilityOwnerId,
                columns, authentication.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sunucular.xlsx")
                .contentType(XLSX_MEDIA_TYPE)
                .body(out.toByteArray());
    }
}
