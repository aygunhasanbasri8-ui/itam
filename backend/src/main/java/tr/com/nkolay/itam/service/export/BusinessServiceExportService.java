package tr.com.nkolay.itam.service.export;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.enums.ServiceCriticality;
import tr.com.nkolay.itam.model.dto.ExportColumnResponse;

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
