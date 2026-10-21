package com.itam.service.export.impl;

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

import com.itam.enums.AssetStatus;
import com.itam.enums.ServiceCriticality;
import com.itam.model.entity.BusinessServiceAsset;
import com.itam.repository.BusinessServiceAssetRepository;
import com.itam.repository.spec.BaseAssetSpecifications;
import com.itam.repository.spec.BusinessServiceAssetSpecifications;
import com.itam.service.export.BusinessServiceExportColumn;
import com.itam.service.export.BusinessServiceExportService;

/** ServerExportServiceImpl ile aynı desen. */
@Service
@Transactional(readOnly = true)
public class BusinessServiceExportServiceImpl implements BusinessServiceExportService {

    private static final int SXSSF_WINDOW_SIZE = 100;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final BusinessServiceAssetRepository businessServiceAssetRepository;

    public BusinessServiceExportServiceImpl(BusinessServiceAssetRepository businessServiceAssetRepository) {
        this.businessServiceAssetRepository = businessServiceAssetRepository;
    }

    @Override
    public void writeXlsx(OutputStream out, ServiceCriticality criticality, AssetStatus status,
            Long responsibilityOwnerId,
            List<String> columns, String requestedBy) {
        List<BusinessServiceExportColumn> resolvedColumns = resolveColumns(columns);

        Specification<BusinessServiceAsset> spec = Specification
                .where(BusinessServiceAssetSpecifications.notDeleted())
                .and(BusinessServiceAssetSpecifications.criticalityEquals(criticality))
                .and(BusinessServiceAssetSpecifications.statusEquals(status))
                .and(BaseAssetSpecifications.responsibilityOwnerEquals(responsibilityOwnerId));

        List<BusinessServiceAsset> services = businessServiceAssetRepository.findAll(spec);

        SXSSFWorkbook workbook = new SXSSFWorkbook(SXSSF_WINDOW_SIZE);
        try {
            Sheet sheet = workbook.createSheet("İş Servisleri");
            int rowIndex = writeMetaBlock(sheet, requestedBy, criticality, status, responsibilityOwnerId);
            rowIndex = writeHeaderRow(sheet, resolvedColumns, rowIndex);
            writeDataRows(sheet, resolvedColumns, services, rowIndex);

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

    private List<BusinessServiceExportColumn> resolveColumns(List<String> requestedCodes) {
        if (requestedCodes == null || requestedCodes.isEmpty()) {
            return List.of(BusinessServiceExportColumn.values());
        }
        List<BusinessServiceExportColumn> resolved = new ArrayList<>();
        for (String code : requestedCodes) {
            resolved.add(BusinessServiceExportColumn.byCode(code));
        }
        return resolved;
    }

    private int writeMetaBlock(Sheet sheet, String requestedBy, ServiceCriticality criticality,
            AssetStatus status, Long responsibilityOwnerId) {
        int rowIndex = 0;
        createTextRow(sheet, rowIndex++, "Üretim tarihi: " + OffsetDateTime.now().format(TIMESTAMP_FORMAT));
        createTextRow(sheet, rowIndex++, "Üreten kullanıcı: " + requestedBy);
        createTextRow(sheet, rowIndex++, "Uygulanan filtreler: " + describeFilters(criticality, status,
                responsibilityOwnerId));
        rowIndex++;
        return rowIndex;
    }

    private String describeFilters(ServiceCriticality criticality, AssetStatus status,
            Long responsibilityOwnerId) {
        List<String> parts = new ArrayList<>();
        if (criticality != null) parts.add("criticality=" + criticality);
        if (status != null) parts.add("status=" + status);
        if (responsibilityOwnerId != null) parts.add("responsibilityOwnerId=" + responsibilityOwnerId);
        return parts.isEmpty() ? "(filtre uygulanmadı)" : String.join(", ", parts);
    }

    private int writeHeaderRow(Sheet sheet, List<BusinessServiceExportColumn> columns, int rowIndex) {
        Row header = sheet.createRow(rowIndex);
        for (int i = 0; i < columns.size(); i++) {
            header.createCell(i).setCellValue(columns.get(i).header());
        }
        return rowIndex + 1;
    }

    private void writeDataRows(Sheet sheet, List<BusinessServiceExportColumn> columns,
            List<BusinessServiceAsset> services, int startRowIndex) {
        int rowIndex = startRowIndex;
        for (BusinessServiceAsset service : services) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < columns.size(); i++) {
                writeCell(row.createCell(i), columns.get(i).valueOf(service));
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
