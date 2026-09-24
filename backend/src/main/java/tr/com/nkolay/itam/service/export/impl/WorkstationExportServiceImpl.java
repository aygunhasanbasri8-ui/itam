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
import tr.com.nkolay.itam.model.entity.WorkstationAsset;
import tr.com.nkolay.itam.repository.WorkstationAssetRepository;
import tr.com.nkolay.itam.repository.spec.WorkstationAssetSpecifications;
import tr.com.nkolay.itam.service.export.WorkstationExportColumn;
import tr.com.nkolay.itam.service.export.WorkstationExportService;

/** ServerExportServiceImpl ile aynı desen — SXSSF, meta blok, hepsi-ya-da-hiçbiri kolon çözümü. */
@Service
@Transactional(readOnly = true)
public class WorkstationExportServiceImpl implements WorkstationExportService {

    private static final int SXSSF_WINDOW_SIZE = 100;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final WorkstationAssetRepository workstationAssetRepository;

    public WorkstationExportServiceImpl(WorkstationAssetRepository workstationAssetRepository) {
        this.workstationAssetRepository = workstationAssetRepository;
    }

    @Override
    public void writeXlsx(OutputStream out, String serialNumber, AssetStatus status, Long departmentId,
            Long assignedPersonId, List<String> columns, String requestedBy) {
        List<WorkstationExportColumn> resolvedColumns = resolveColumns(columns);

        Specification<WorkstationAsset> spec = Specification
                .where(WorkstationAssetSpecifications.notDeleted())
                .and(WorkstationAssetSpecifications.serialNumberContains(serialNumber))
                .and(WorkstationAssetSpecifications.statusEquals(status))
                .and(WorkstationAssetSpecifications.departmentEquals(departmentId))
                .and(WorkstationAssetSpecifications.assignedPersonEquals(assignedPersonId));

        List<WorkstationAsset> workstations = workstationAssetRepository.findAll(spec);

        SXSSFWorkbook workbook = new SXSSFWorkbook(SXSSF_WINDOW_SIZE);
        try {
            Sheet sheet = workbook.createSheet("İş İstasyonları");
            int rowIndex = writeMetaBlock(sheet, requestedBy, serialNumber, status, departmentId, assignedPersonId);
            rowIndex = writeHeaderRow(sheet, resolvedColumns, rowIndex);
            writeDataRows(sheet, resolvedColumns, workstations, rowIndex);

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

    private List<WorkstationExportColumn> resolveColumns(List<String> requestedCodes) {
        if (requestedCodes == null || requestedCodes.isEmpty()) {
            return List.of(WorkstationExportColumn.values());
        }
        List<WorkstationExportColumn> resolved = new ArrayList<>();
        for (String code : requestedCodes) {
            resolved.add(WorkstationExportColumn.byCode(code));
        }
        return resolved;
    }

    private int writeMetaBlock(Sheet sheet, String requestedBy, String serialNumber, AssetStatus status,
            Long departmentId, Long assignedPersonId) {
        int rowIndex = 0;
        createTextRow(sheet, rowIndex++, "Üretim tarihi: " + OffsetDateTime.now().format(TIMESTAMP_FORMAT));
        createTextRow(sheet, rowIndex++, "Üreten kullanıcı: " + requestedBy);
        createTextRow(sheet, rowIndex++, "Uygulanan filtreler: "
                + describeFilters(serialNumber, status, departmentId, assignedPersonId));
        rowIndex++;
        return rowIndex;
    }

    private String describeFilters(String serialNumber, AssetStatus status, Long departmentId,
            Long assignedPersonId) {
        List<String> parts = new ArrayList<>();
        if (serialNumber != null && !serialNumber.isBlank()) parts.add("serialNumber~" + serialNumber);
        if (status != null) parts.add("status=" + status);
        if (departmentId != null) parts.add("departmentId=" + departmentId);
        if (assignedPersonId != null) parts.add("assignedPersonId=" + assignedPersonId);
        return parts.isEmpty() ? "(filtre uygulanmadı)" : String.join(", ", parts);
    }

    private int writeHeaderRow(Sheet sheet, List<WorkstationExportColumn> columns, int rowIndex) {
        Row header = sheet.createRow(rowIndex);
        for (int i = 0; i < columns.size(); i++) {
            header.createCell(i).setCellValue(columns.get(i).header());
        }
        return rowIndex + 1;
    }

    private void writeDataRows(Sheet sheet, List<WorkstationExportColumn> columns,
            List<WorkstationAsset> workstations, int startRowIndex) {
        int rowIndex = startRowIndex;
        for (WorkstationAsset workstation : workstations) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < columns.size(); i++) {
                writeCell(row.createCell(i), columns.get(i).valueOf(workstation));
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
