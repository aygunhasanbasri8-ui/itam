package tr.com.nkolay.itam.service.audit;

import java.util.List;

import tr.com.nkolay.itam.model.dto.AssetRevisionResponse;

public interface AssetAuditService {

    /** Bir asset'in oluşturulduğundan bu yana geçirdiği tüm revizyonları eskiden yeniye sıralı döner. */
    List<AssetRevisionResponse> getHistory(Long assetId);
}
