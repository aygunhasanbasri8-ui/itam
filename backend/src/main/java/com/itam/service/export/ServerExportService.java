package com.itam.service.export;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import com.itam.enums.AssetStatus;
import com.itam.enums.ServerEnvironment;
import com.itam.model.dto.ExportColumnResponse;

public interface ServerExportService {

    /**
     * @param columns    aktarılacak kolon kodları; boş/null ise tüm kolonlar yazılır
     *                   (bkz. {@link ServerExportColumn} - kod listesi orada)
     * @param requestedBy başlık bloğuna yazılacak, dosyayı üreten kullanıcı
     */
    void writeXlsx(OutputStream out, String hostname, ServerEnvironment environment,
            AssetStatus status, Long responsibilityOwnerId, List<String> columns, String requestedBy);

    default List<ExportColumnResponse> columns() {
        return Arrays.stream(ServerExportColumn.values())
                .map(column -> new ExportColumnResponse(column.code(), column.header()))
                .toList();
    }
}
