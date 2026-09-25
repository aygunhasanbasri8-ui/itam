package tr.com.nkolay.itam.mapper;

import org.springframework.stereotype.Component;

import tr.com.nkolay.itam.model.dto.AssetDependencyResponse;
import tr.com.nkolay.itam.model.entity.AssetDependency;

@Component
public class AssetDependencyMapper {

    public AssetDependencyResponse toResponse(AssetDependency dependency) {
        return new AssetDependencyResponse(
                dependency.getId(),
                dependency.getParentAsset().getId(),
                dependency.getParentAsset().getAssetName(),
                dependency.getChildAsset().getId(),
                dependency.getChildAsset().getAssetName(),
                dependency.getDependencyType(),
                dependency.getNotes(),
                dependency.getCreatedAt(),
                dependency.getCreatedBy());
    }
}
