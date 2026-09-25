package tr.com.nkolay.itam.service.customfield.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.exception.ResourceNotFoundException;
import tr.com.nkolay.itam.mapper.CustomFieldDefinitionMapper;
import tr.com.nkolay.itam.model.dto.CustomFieldDefinitionRequest;
import tr.com.nkolay.itam.model.dto.CustomFieldDefinitionResponse;
import tr.com.nkolay.itam.model.dto.CustomFieldDefinitionUpdateRequest;
import tr.com.nkolay.itam.model.entity.AssetTypeDefinition;
import tr.com.nkolay.itam.model.entity.CustomFieldDefinition;
import tr.com.nkolay.itam.repository.AssetTypeDefinitionRepository;
import tr.com.nkolay.itam.repository.CustomFieldDefinitionRepository;
import tr.com.nkolay.itam.service.customfield.CustomFieldDefinitionService;
import tr.com.nkolay.itam.validation.ValidationCollector;

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
