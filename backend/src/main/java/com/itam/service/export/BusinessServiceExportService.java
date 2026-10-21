package com.itam.service.export;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import com.itam.enums.AssetStatus;
import com.itam.enums.ServiceCriticality;
import com.itam.model.dto.ExportColumnResponse;

public interface BusinessServiceExportService {

    void writeXlsx(OutputStream out, ServiceCriticality criticality, AssetStatus status,
            Long responsibilityOwnerId,
            List<String> columns, String requestedBy);

    default List<ExportColumnResponse> columns() {
        return Arrays.stream(BusinessServiceExportColumn.values())
                .map(column -> new ExportColumnResponse(column.code(), column.header()))
                .toList();
    }
}
