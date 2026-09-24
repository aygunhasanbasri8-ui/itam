package tr.com.nkolay.itam.service.export;

import java.io.OutputStream;
import java.util.List;

import tr.com.nkolay.itam.enums.AssetStatus;

public interface WorkstationExportService {

    void writeXlsx(OutputStream out, String serialNumber, AssetStatus status, Long departmentId,
            Long assignedPersonId, List<String> columns, String requestedBy);
}
