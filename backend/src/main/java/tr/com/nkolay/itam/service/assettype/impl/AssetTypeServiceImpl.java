package tr.com.nkolay.itam.service.assettype.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.exception.ResourceNotFoundException;
import tr.com.nkolay.itam.mapper.AssetTypeMapper;
import tr.com.nkolay.itam.model.dto.AssetTypeResponse;
import tr.com.nkolay.itam.model.dto.AssetTypeUpdateRequest;
import tr.com.nkolay.itam.model.entity.AssetTypeDefinition;
import tr.com.nkolay.itam.repository.AssetTypeDefinitionRepository;
import tr.com.nkolay.itam.service.assettype.AssetTypeService;

@Service
@Transactional
public class AssetTypeServiceImpl implements AssetTypeService {

    private final AssetTypeDefinitionRepository repository;
    private final AssetTypeMapper mapper;

    public AssetTypeServiceImpl(AssetTypeDefinitionRepository repository, AssetTypeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetTypeResponse> list() {
        return repository.findAll(Sort.by("sortOrder")).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public AssetTypeResponse update(String code, AssetTypeUpdateRequest request) {
        AssetTypeDefinition type = repository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Asset tipi", code));

        type.setDisplayName(request.displayName().trim());
        type.setIcon(request.icon().trim());
        type.setDescription(request.description() == null || request.description().isBlank()
                ? null : request.description().trim());
        type.setSortOrder(request.sortOrder());
        return mapper.toResponse(type);
    }
}
