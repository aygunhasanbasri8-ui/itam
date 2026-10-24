package com.itam.service.importer;

import java.io.InputStream;
import java.io.OutputStream;

import com.itam.model.dto.ImportReport;

/**
 * Bir asset tipini Excel'den toplu ekler. Tip, oluşturma isteğinin tipiyle ayrılıyor:
 * {@code AssetImportService<ServerAssetRequest>} sunucuları içe aktarır.
 * <p>
 * Önce tüm dosya doğrulanır; tek bir satırda bile hata varsa hiçbir kayıt yazılmaz
 * ve satır satır hata raporu döner. Yarım kalmış bir yükleme envanteri hangi
 * satırın girip hangisinin girmediği belli olmayan bir duruma sokardı.
 */
public interface AssetImportService<R> {

    ImportReport importXlsx(InputStream xlsx, boolean dryRun);

    /** Doldurulacak boş şablon: başlık satırı export'la aynı. */
    void writeTemplate(OutputStream out);
}
