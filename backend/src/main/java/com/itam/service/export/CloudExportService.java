package com.itam.service.export;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import com.itam.enums.AssetStatus;
import com.itam.enums.CloudProvider;
import com.itam.model.dto.ExportColumnResponse;

public interface CloudExportService {

    void writeXlsx(OutputStream out, CloudProvider provider, String region, AssetStatus status,
            Long responsibilityOwnerId,
            List<String> columns, String requestedBy);

    default List<ExportColumnResponse> columns() {
        return Arrays.stream(CloudExportColumn.values())
                .map(column -> new ExportColumnResponse(column.code(), column.header()))
                .toList();
    }
}
