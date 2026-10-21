package tr.com.nkolay.itam.service.export;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.model.dto.ExportColumnResponse;

public interface WorkstationExportService {

    void writeXlsx(OutputStream out, String serialNumber, AssetStatus status, Long departmentId,
            Long assignedPersonId, List<String> columns, String requestedBy);

    default List<ExportColumnResponse> columns() {
        return Arrays.stream(WorkstationExportColumn.values())
                .map(column -> new ExportColumnResponse(column.code(), column.header()))
                .toList();
    }
}
