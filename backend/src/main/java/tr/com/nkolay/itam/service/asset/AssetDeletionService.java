package tr.com.nkolay.itam.service.asset;

public interface AssetDeletionService {

    /**
     * Kaydı fiziksel olarak silmez, is_deleted işaretler; denetim geçmişi ve
     * eski Excel çıktılarındaki id'ler anlamını korusun diye. Asset'e dokunan
     * bir bağımlılık varsa reddedilir, önce bağımlılıkların kaldırılması gerekir.
     * <p>
     * Bilinen kısıt: hostname ve seri no tekil indeksleri silinmiş kayıtları da
     * kapsıyor (is_deleted base_asset'te, indeks alt tablodayken kısmi indeks
     * kurulamıyor). Aynı hostname ile yeni kayıt açılırsa 409 döner.
     */
    void softDelete(Long assetId);
}
