package com.itam.service.export;

import java.io.OutputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import com.itam.enums.AssetStatus;
import com.itam.model.dto.ExportColumnResponse;

public interface ApplicationExportService {

    void writeXlsx(OutputStream out, String vendor, AssetStatus status, Long responsibilityOwnerId,
            LocalDate expiryBefore,
            List<String> columns, String requestedBy);

    default List<ExportColumnResponse> columns() {
        return Arrays.stream(ApplicationExportColumn.values())
                .map(column -> new ExportColumnResponse(column.code(), column.header()))
                .toList();
    }
}
