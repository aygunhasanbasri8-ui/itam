package tr.com.nkolay.itam.service.asset;

import java.util.List;

import tr.com.nkolay.itam.model.dto.AssetSummaryResponse;

public interface AssetSearchService {

    /** Silinmemiş asset'ler içinde adı q'yu içerenler, ada göre sıralı; q boşsa ilk kayıtlar. */
    List<AssetSummaryResponse> search(String q, int limit);
}
