package tr.com.nkolay.itam.service.export.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.enums.ServerEnvironment;
import tr.com.nkolay.itam.model.entity.ServerAsset;
import tr.com.nkolay.itam.repository.ServerAssetRepository;
import tr.com.nkolay.itam.repository.spec.BaseAssetSpecifications;
import tr.com.nkolay.itam.repository.spec.ServerAssetSpecifications;
import tr.com.nkolay.itam.service.export.ServerExportColumn;
import tr.com.nkolay.itam.service.export.ServerExportService;

/**
 * SXSSF akış tabanlı yazar kullanıyor: satırlar diske arka planda dökülür,
 * kayıt sayısı artsa da bellekte aynı anda küçük bir pencere (100 satır)
 * tutulur. Ham POI API'si (autoSizeColumn gibi) rastgele erişim ister,
 * SXSSF ile karışmaz - bu yüzden burada sadece sırayla yazılıyor.
 */
@Service
@Transactional(readOnly = true)
public class ServerExportServiceImpl implements ServerExportService {

    private static final int SXSSF_WINDOW_SIZE = 100;
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final ServerAssetRepository serverAssetRepository;

    public ServerExportServiceImpl(ServerAssetRepository serverAssetRepository) {
        this.serverAssetRepository = serverAssetRepository;
    }

    @Override
    public void writeXlsx(OutputStream out, String hostname, ServerEnvironment environment,
            AssetStatus status, Long responsibilityOwnerId, List<String> columns, String requestedBy) {
        List<ServerExportColumn> resolvedColumns = resolveColumns(columns);

        Specification<ServerAsset> spec = Specification
                .where(ServerAssetSpecifications.notDeleted())
                .and(ServerAssetSpecifications.hostnameContains(hostname))
                .and(ServerAssetSpecifications.environmentEquals(environment))
                .and(ServerAssetSpecifications.statusEquals(status))
                .and(BaseAssetSpecifications.responsibilityOwnerEquals(responsibilityOwnerId));

        List<ServerAsset> servers = serverAssetRepository.findAll(spec);

        SXSSFWorkbook workbook = new SXSSFWorkbook(SXSSF_WINDOW_SIZE);
        try {
            Sheet sheet = workbook.createSheet("Sunucular");
            int rowIndex = writeMetaBlock(sheet, requestedBy, hostname, environment, status, responsibilityOwnerId);
            rowIndex = writeHeaderRow(sheet, resolvedColumns, rowIndex);
            writeDataRows(sheet, resolvedColumns, servers, rowIndex);

            workbook.write(out);
        } catch (IOException e) {
            throw new UncheckedIOException("Excel dosyası üretilemedi: " + e.getMessage(), e);
        } finally {
            workbook.dispose();
            try {
                workbook.close();
            } catch (IOException ignored) {
                // dispose() zaten geçici dosyaları temizledi, close() burada sadece kaynak serbest bırakır
            }
        }
    }

    private List<ServerExportColumn> resolveColumns(List<String> requestedCodes) {
        if (requestedCodes == null || requestedCodes.isEmpty()) {
            return List.of(ServerExportColumn.values());
        }
        List<ServerExportColumn> resolved = new ArrayList<>();
        for (String code : requestedCodes) {
            resolved.add(ServerExportColumn.byCode(code));
        }
        return resolved;
    }

    /** Denetim standardı: dosyanın hangi koşullarda üretildiği kendi içinde okunabilsin. */
    private int writeMetaBlock(Sheet sheet, String requestedBy, String hostname,
            ServerEnvironment environment, AssetStatus status, Long responsibilityOwnerId) {
        int rowIndex = 0;
        createTextRow(sheet, rowIndex++, "Üretim tarihi: " + OffsetDateTime.now().format(TIMESTAMP_FORMAT));
        createTextRow(sheet, rowIndex++, "Üreten kullanıcı: " + requestedBy);
        createTextRow(sheet, rowIndex++, "Uygulanan filtreler: " + describeFilters(hostname, environment,
                status, responsibilityOwnerId));
        rowIndex++; // başlık bloğunu veriden ayıran boş satır
        return rowIndex;
    }

    private String describeFilters(String hostname, ServerEnvironment environment,
            AssetStatus status, Long responsibilityOwnerId) {
        List<String> parts = new ArrayList<>();
        if (hostname != null && !hostname.isBlank()) parts.add("hostname~" + hostname);
        if (environment != null) parts.add("environment=" + environment);
        if (status != null) parts.add("status=" + status);
        if (responsibilityOwnerId != null) parts.add("responsibilityOwnerId=" + responsibilityOwnerId);
        return parts.isEmpty() ? "(filtre uygulanmadı)" : String.join(", ", parts);
    }

    private int writeHeaderRow(Sheet sheet, List<ServerExportColumn> columns, int rowIndex) {
        Row header = sheet.createRow(rowIndex);
        for (int i = 0; i < columns.size(); i++) {
            header.createCell(i).setCellValue(columns.get(i).header());
        }
        return rowIndex + 1;
    }

    private void writeDataRows(Sheet sheet, List<ServerExportColumn> columns,
            List<ServerAsset> servers, int startRowIndex) {
        int rowIndex = startRowIndex;
        for (ServerAsset server : servers) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < columns.size(); i++) {
                writeCell(row.createCell(i), columns.get(i).valueOf(server));
            }
        }
    }

    private void createTextRow(Sheet sheet, int rowIndex, String text) {
        sheet.createRow(rowIndex).createCell(0).setCellValue(text);
    }

    private void writeCell(Cell cell, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
    }
}
