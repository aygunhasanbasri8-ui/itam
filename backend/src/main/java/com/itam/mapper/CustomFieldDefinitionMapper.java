package com.itam.mapper;

import org.springframework.stereotype.Component;

import com.itam.model.dto.CustomFieldDefinitionResponse;
import com.itam.model.entity.CustomFieldDefinition;

@Component
public class CustomFieldDefinitionMapper {

    public CustomFieldDefinitionResponse toResponse(CustomFieldDefinition definition) {
        return new CustomFieldDefinitionResponse(
                definition.getId(),
                definition.getAssetType().getCode(),
                definition.getFieldKey(),
                definition.getLabel(),
                definition.getDataType(),
                definition.isShowInList(),
                definition.isActive(),
                definition.getSortOrder(),
                definition.getCreatedAt(),
                definition.getUpdatedAt());
    }
}
