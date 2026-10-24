package com.itam.service.asset;

public interface AssetDeletionService {

    /**
     * Kaydı fiziksel olarak silmez, is_deleted işaretler; denetim geçmişi ve
     * eski Excel çıktılarındaki id'ler anlamını korusun diye. Asset'e dokunan
     * bir bağımlılık varsa reddedilir, önce bağımlılıkların kaldırılması gerekir.
     * <p>
     * Silinen kaydın hostname'i / seri numarası tekil indekslerin dışında kalır
     * (V10), aynı değerle yeni kayıt açılabilir.
     */
    void softDelete(Long assetId);
}
