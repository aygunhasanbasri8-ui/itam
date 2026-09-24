package tr.com.nkolay.itam.service.export;

import java.io.OutputStream;
import java.util.List;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.enums.ServiceCriticality;

public interface BusinessServiceExportService {

    void writeXlsx(OutputStream out, ServiceCriticality criticality, AssetStatus status,
            List<String> columns, String requestedBy);
}
