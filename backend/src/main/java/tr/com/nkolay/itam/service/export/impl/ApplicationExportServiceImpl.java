package tr.com.nkolay.itam.service.export.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
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
import tr.com.nkolay.itam.model.entity.ApplicationAsset;
import tr.com.nkolay.itam.repository.ApplicationAssetRepository;
import tr.com.nkolay.itam.repository.spec.ApplicationAssetSpecifications;
import tr.com.nkolay.itam.service.export.ApplicationExportColumn;
import tr.com.nkolay.itam.service.export.ApplicationExportService;

/** ServerExportServiceImpl ile aynı desen. */
@Service
@Transactional(readOnly = true)
public class ApplicationExportServiceImpl implements ApplicationExportService {

    private static final int SXSSF_WINDOW_SIZE = 100;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final ApplicationAssetRepository applicationAssetRepository;

    public ApplicationExportServiceImpl(ApplicationAssetRepository applicationAssetRepository) {
        this.applicationAssetRepository = applicationAssetRepository;
    }

    @Override
    public void writeXlsx(OutputStream out, String vendor, AssetStatus status, LocalDate expiryBefore,
            List<String> columns, String requestedBy) {
        List<ApplicationExportColumn> resolvedColumns = resolveColumns(columns);

        Specification<ApplicationAsset> spec = Specification
                .where(ApplicationAssetSpecifications.notDeleted())
                .and(ApplicationAssetSpecifications.vendorContains(vendor))
                .and(ApplicationAssetSpecifications.statusEquals(status))
                .and(ApplicationAssetSpecifications.expiryBefore(expiryBefore));

        List<ApplicationAsset> applications = applicationAssetRepository.findAll(spec);

        SXSSFWorkbook workbook = new SXSSFWorkbook(SXSSF_WINDOW_SIZE);
        try {
            Sheet sheet = workbook.createSheet("Uygulama-Yazılım");
            int rowIndex = writeMetaBlock(sheet, requestedBy, vendor, status, expiryBefore);
            rowIndex = writeHeaderRow(sheet, resolvedColumns, rowIndex);
            writeDataRows(sheet, resolvedColumns, applications, rowIndex);

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

    private List<ApplicationExportColumn> resolveColumns(List<String> requestedCodes) {
        if (requestedCodes == null || requestedCodes.isEmpty()) {
            return List.of(ApplicationExportColumn.values());
        }
        List<ApplicationExportColumn> resolved = new ArrayList<>();
        for (String code : requestedCodes) {
            resolved.add(ApplicationExportColumn.byCode(code));
        }
        return resolved;
    }

    private int writeMetaBlock(Sheet sheet, String requestedBy, String vendor, AssetStatus status,
            LocalDate expiryBefore) {
        int rowIndex = 0;
        createTextRow(sheet, rowIndex++, "Üretim tarihi: " + OffsetDateTime.now().format(TIMESTAMP_FORMAT));
        createTextRow(sheet, rowIndex++, "Üreten kullanıcı: " + requestedBy);
        createTextRow(sheet, rowIndex++, "Uygulanan filtreler: " + describeFilters(vendor, status, expiryBefore));
        rowIndex++;
        return rowIndex;
    }

    private String describeFilters(String vendor, AssetStatus status, LocalDate expiryBefore) {
        List<String> parts = new ArrayList<>();
        if (vendor != null && !vendor.isBlank()) parts.add("vendor~" + vendor);
        if (status != null) parts.add("status=" + status);
        if (expiryBefore != null) parts.add("expiryBefore=" + expiryBefore);
        return parts.isEmpty() ? "(filtre uygulanmadı)" : String.join(", ", parts);
    }

    private int writeHeaderRow(Sheet sheet, List<ApplicationExportColumn> columns, int rowIndex) {
        Row header = sheet.createRow(rowIndex);
        for (int i = 0; i < columns.size(); i++) {
            header.createCell(i).setCellValue(columns.get(i).header());
        }
        return rowIndex + 1;
    }

    private void writeDataRows(Sheet sheet, List<ApplicationExportColumn> columns,
            List<ApplicationAsset> applications, int startRowIndex) {
        int rowIndex = startRowIndex;
        for (ApplicationAsset application : applications) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < columns.size(); i++) {
                writeCell(row.createCell(i), columns.get(i).valueOf(application));
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
