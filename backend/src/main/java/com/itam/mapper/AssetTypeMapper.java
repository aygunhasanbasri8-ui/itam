package com.itam.mapper;

import org.springframework.stereotype.Component;

import com.itam.model.dto.AssetTypeResponse;
import com.itam.model.entity.AssetTypeDefinition;

@Component
public class AssetTypeMapper {

    public AssetTypeResponse toResponse(AssetTypeDefinition type) {
        return new AssetTypeResponse(
                type.getCode(),
                type.getDisplayName(),
                type.getKind(),
                type.getIcon(),
                type.getDescription(),
                type.isSystem(),
                type.isActive(),
                type.getSortOrder());
    }
}
