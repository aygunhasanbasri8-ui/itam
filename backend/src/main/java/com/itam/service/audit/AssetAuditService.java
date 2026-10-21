package com.itam.service.audit;

import java.util.List;

import com.itam.model.dto.AssetRevisionResponse;

public interface AssetAuditService {

    /** Bir asset'in oluşturulduğundan bu yana geçirdiği tüm revizyonları eskiden yeniye sıralı döner. */
    List<AssetRevisionResponse> getHistory(Long assetId);
}
