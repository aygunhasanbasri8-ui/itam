package com.itam.mapper;

import org.springframework.stereotype.Component;

import com.itam.model.dto.AssetDependencyResponse;
import com.itam.model.entity.AssetDependency;

@Component
public class AssetDependencyMapper {

    public AssetDependencyResponse toResponse(AssetDependency dependency) {
        return new AssetDependencyResponse(
                dependency.getId(),
                dependency.getParentAsset().getId(),
                dependency.getParentAsset().getAssetName(),
                dependency.getParentAsset().getAssetType().getCode(),
                dependency.getChildAsset().getId(),
                dependency.getChildAsset().getAssetName(),
                dependency.getChildAsset().getAssetType().getCode(),
                dependency.getDependencyType(),
                dependency.getNotes(),
                dependency.getCreatedAt(),
                dependency.getCreatedBy());
    }
}
