package tr.com.nkolay.itam.service.export;

import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

import tr.com.nkolay.itam.enums.AssetStatus;

public interface ApplicationExportService {

    void writeXlsx(OutputStream out, String vendor, AssetStatus status, LocalDate expiryBefore,
            List<String> columns, String requestedBy);
}
