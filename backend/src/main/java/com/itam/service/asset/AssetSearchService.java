package com.itam.service.asset;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.itam.enums.AssetStatus;
import com.itam.model.dto.AssetSummaryResponse;
import com.itam.model.dto.PageResponse;

public interface AssetSearchService {

    /** Silinmemiş asset'ler içinde adı q'yu içerenler, ada göre sıralı; q boşsa ilk kayıtlar. */
    List<AssetSummaryResponse> search(String q, int limit);

    /** Beş tipin hepsini kapsayan sayfalı liste; boş bırakılan filtre uygulanmaz. */
    PageResponse<AssetSummaryResponse> list(String q, String assetTypeCode, AssetStatus status,
            Long responsibilityOwnerId, Pageable pageable);
}
