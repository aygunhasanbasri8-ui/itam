package com.itam.service.customfield.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itam.exception.ResourceNotFoundException;
import com.itam.mapper.CustomFieldDefinitionMapper;
import com.itam.model.dto.CustomFieldDefinitionRequest;
import com.itam.model.dto.CustomFieldDefinitionResponse;
import com.itam.model.dto.CustomFieldDefinitionUpdateRequest;
import com.itam.model.entity.AssetTypeDefinition;
import com.itam.model.entity.CustomFieldDefinition;
import com.itam.repository.AssetTypeDefinitionRepository;
import com.itam.repository.CustomFieldDefinitionRepository;
import com.itam.service.customfield.CustomFieldDefinitionService;
import com.itam.validation.ValidationCollector;

@Service
@Transactional
public class CustomFieldDefinitionServiceImpl implements CustomFieldDefinitionService {

    private final CustomFieldDefinitionRepository definitionRepository;
    private final AssetTypeDefinitionRepository assetTypeDefinitionRepository;
    private final CustomFieldDefinitionMapper mapper;

    public CustomFieldDefinitionServiceImpl(CustomFieldDefinitionRepository definitionRepository,
            AssetTypeDefinitionRepository assetTypeDefinitionRepository,
            CustomFieldDefinitionMapper mapper) {
        this.definitionRepository = definitionRepository;
        this.assetTypeDefinitionRepository = assetTypeDefinitionRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomFieldDefinitionResponse> listByAssetType(String assetTypeCode) {
        return definitionRepository.findByAssetTypeCodeOrderBySortOrder(assetTypeCode).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public CustomFieldDefinitionResponse create(CustomFieldDefinitionRequest request) {
        ValidationCollector errors = ValidationCollector.create();

        AssetTypeDefinition assetType = assetTypeDefinitionRepository.findById(request.assetTypeCode())
                .orElse(null);
        if (assetType == null) {
            errors.add("assetTypeCode", "tanimsizTip", "Tanımlı olmayan asset tipi", request.assetTypeCode());
        }

        errors.check(!definitionRepository.existsByAssetTypeCodeAndFieldKey(
                        request.assetTypeCode(), request.fieldKey()),
                "fieldKey", "tekil", "Bu asset tipinde aynı anahtarla bir alan zaten tanımlı",
                request.fieldKey());
        errors.throwIfInvalid();

        CustomFieldDefinition definition = new CustomFieldDefinition();
        definition.setAssetType(assetType);
        definition.setFieldKey(request.fieldKey());
        definition.setLabel(request.label());
        definition.setDataType(request.dataType());
        definition.setShowInList(request.showInList());
        definition.setSortOrder(request.sortOrder());

        return mapper.toResponse(definitionRepository.save(definition));
    }

    @Override
    public CustomFieldDefinitionResponse update(Long id, CustomFieldDefinitionUpdateRequest request) {
        CustomFieldDefinition definition = definitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Özel alan tanımı", id));

        definition.setLabel(request.label());
        definition.setShowInList(request.showInList());
        definition.setActive(request.active());
        definition.setSortOrder(request.sortOrder());

        return mapper.toResponse(definition);
    }
}
