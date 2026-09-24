package tr.com.nkolay.itam.service.export;

import java.io.OutputStream;
import java.util.List;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.enums.ServerEnvironment;

public interface ServerExportService {

    /**
     * @param columns    aktarılacak kolon kodları; boş/null ise tüm kolonlar yazılır
     *                   (bkz. {@link ServerExportColumn} - kod listesi orada)
     * @param requestedBy başlık bloğuna yazılacak, dosyayı üreten kullanıcı
     */
    void writeXlsx(OutputStream out, String hostname, ServerEnvironment environment,
            AssetStatus status, Long responsibilityOwnerId, List<String> columns, String requestedBy);
}
