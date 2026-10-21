package com.itam.service.asset;

import com.itam.model.dto.BulkStatusUpdateRequest;
import com.itam.model.dto.BulkStatusUpdateResponse;

public interface AssetBulkService {

    /**
     * Tüm kayıtlar geçerliyse hepsi güncellenir; herhangi biri geçersizse
     * (bulunamıyor veya silinmiş) hiçbiri güncellenmez ve hangi id'lerin
     * neden reddedildiği ValidationException içinde döner.
     */
    BulkStatusUpdateResponse updateStatus(BulkStatusUpdateRequest request);
}
