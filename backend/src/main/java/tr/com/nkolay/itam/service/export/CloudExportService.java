package tr.com.nkolay.itam.service.export;

import java.io.OutputStream;
import java.util.List;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.enums.CloudProvider;

public interface CloudExportService {

    void writeXlsx(OutputStream out, CloudProvider provider, String region, AssetStatus status,
            List<String> columns, String requestedBy);
}
