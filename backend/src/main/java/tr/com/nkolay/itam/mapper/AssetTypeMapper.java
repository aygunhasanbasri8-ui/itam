package tr.com.nkolay.itam.mapper;

import org.springframework.stereotype.Component;

import tr.com.nkolay.itam.model.dto.AssetTypeResponse;
import tr.com.nkolay.itam.model.entity.AssetTypeDefinition;

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
