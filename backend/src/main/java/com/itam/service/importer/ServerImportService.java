package com.itam.service.importer;

import java.io.InputStream;
import java.io.OutputStream;

import com.itam.model.dto.ImportReport;

/**
 * Sunucuları Excel'den toplu ekler (Faz 7).
 * <p>
 * Önce tüm dosya doğrulanır; tek bir satırda bile hata varsa hiçbir kayıt yazılmaz
 * ve satır satır hata raporu döner. Yarım kalmış bir yükleme envanteri hangi
 * satırın girip hangisinin girmediği belli olmayan bir duruma sokardı.
 */
public interface ServerImportService {

    ImportReport importXlsx(InputStream xlsx, boolean dryRun);

    /** Doldurulacak boş şablon: başlık satırı export'la aynı. */
    void writeTemplate(OutputStream out);
}
