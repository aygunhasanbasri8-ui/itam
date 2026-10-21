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
import tr.com.nkolay.itam.enums.CloudProvider;
import tr.com.nkolay.itam.model.entity.CloudAsset;
import tr.com.nkolay.itam.repository.CloudAssetRepository;
import tr.com.nkolay.itam.repository.spec.BaseAssetSpecifications;
import tr.com.nkolay.itam.repository.spec.CloudAssetSpecifications;
import tr.com.nkolay.itam.service.export.CloudExportColumn;
import tr.com.nkolay.itam.service.export.CloudExportService;

/** ServerExportServiceImpl ile aynı desen. */
@Service
@Transactional(readOnly = true)
public class CloudExportServiceImpl implements CloudExportService {

    private static final int SXSSF_WINDOW_SIZE = 100;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final CloudAssetRepository cloudAssetRepository;

    public CloudExportServiceImpl(CloudAssetRepository cloudAssetRepository) {
        this.cloudAssetRepository = cloudAssetRepository;
    }

    @Override
    public void writeXlsx(OutputStream out, CloudProvider provider, String region, AssetStatus status,
            Long responsibilityOwnerId,
            List<String> columns, String requestedBy) {
        List<CloudExportColumn> resolvedColumns = resolveColumns(columns);

        Specification<CloudAsset> spec = Specification
                .where(CloudAssetSpecifications.notDeleted())
                .and(CloudAssetSpecifications.providerEquals(provider))
                .and(CloudAssetSpecifications.regionEquals(region))
                .and(CloudAssetSpecifications.statusEquals(status))
                .and(BaseAssetSpecifications.responsibilityOwnerEquals(responsibilityOwnerId));

        List<CloudAsset> clouds = cloudAssetRepository.findAll(spec);

        SXSSFWorkbook workbook = new SXSSFWorkbook(SXSSF_WINDOW_SIZE);
        try {
            Sheet sheet = workbook.createSheet("Cloud Varlıkları");
            int rowIndex = writeMetaBlock(sheet, requestedBy, provider, region, status,
                    responsibilityOwnerId);
            rowIndex = writeHeaderRow(sheet, resolvedColumns, rowIndex);
            writeDataRows(sheet, resolvedColumns, clouds, rowIndex);

            workbook.write(out);
        } catch (IOException e) {
            throw new UncheckedIOException("Excel dosyası üretilemedi: " + e.getMessage(), e);
        } finally {
            workbook.dispose();
            try {
                workbook.close();
            } catch (IOException ignored) {
                // dispose() zaten gecici dosyalari temizledi
            }
        }
    }

    private List<CloudExportColumn> resolveColumns(List<String> requestedCodes) {
        if (requestedCodes == null || requestedCodes.isEmpty()) {
            return List.of(CloudExportColumn.values());
        }
        List<CloudExportColumn> resolved = new ArrayList<>();
        for (String code : requestedCodes) {
            resolved.add(CloudExportColumn.byCode(code));
        }
        return resolved;
    }

    private int writeMetaBlock(Sheet sheet, String requestedBy, CloudProvider provider, String region,
            AssetStatus status, Long responsibilityOwnerId) {
        int rowIndex = 0;
        createTextRow(sheet, rowIndex++, "Üretim tarihi: " + OffsetDateTime.now().format(TIMESTAMP_FORMAT));
        createTextRow(sheet, rowIndex++, "Üreten kullanıcı: " + requestedBy);
        createTextRow(sheet, rowIndex++, "Uygulanan filtreler: " + describeFilters(provider, region,
                status, responsibilityOwnerId));
        rowIndex++;
        return rowIndex;
    }

    private String describeFilters(CloudProvider provider, String region, AssetStatus status,
            Long responsibilityOwnerId) {
        List<String> parts = new ArrayList<>();
        if (provider != null) parts.add("provider=" + provider);
        if (region != null && !region.isBlank()) parts.add("region=" + region);
        if (status != null) parts.add("status=" + status);
        if (responsibilityOwnerId != null) parts.add("responsibilityOwnerId=" + responsibilityOwnerId);
        return parts.isEmpty() ? "(filtre uygulanmadı)" : String.join(", ", parts);
    }

    private int writeHeaderRow(Sheet sheet, List<CloudExportColumn> columns, int rowIndex) {
        Row header = sheet.createRow(rowIndex);
        for (int i = 0; i < columns.size(); i++) {
            header.createCell(i).setCellValue(columns.get(i).header());
        }
        return rowIndex + 1;
    }

    private void writeDataRows(Sheet sheet, List<CloudExportColumn> columns, List<CloudAsset> clouds,
            int startRowIndex) {
        int rowIndex = startRowIndex;
        for (CloudAsset cloud : clouds) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < columns.size(); i++) {
                writeCell(row.createCell(i), columns.get(i).valueOf(cloud));
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
