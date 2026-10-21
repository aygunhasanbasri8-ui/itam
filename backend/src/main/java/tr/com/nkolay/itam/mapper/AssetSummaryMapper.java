package tr.com.nkolay.itam.mapper;

import org.springframework.stereotype.Component;

import tr.com.nkolay.itam.model.dto.AssetSummaryResponse;
import tr.com.nkolay.itam.model.entity.BaseAsset;

@Component
public class AssetSummaryMapper {

    public AssetSummaryResponse toSummary(BaseAsset asset) {
        return new AssetSummaryResponse(
                asset.getId(),
                asset.getAssetName(),
                asset.getAssetType().getCode(),
                asset.getStatus(),
                asset.getCreatedAt());
    }
}
