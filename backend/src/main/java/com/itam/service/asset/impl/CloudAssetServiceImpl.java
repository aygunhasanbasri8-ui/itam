package com.itam.service.asset.impl;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itam.enums.AssetStatus;
import com.itam.enums.CloudProvider;
import com.itam.exception.ResourceNotFoundException;
import com.itam.mapper.CloudAssetMapper;
import com.itam.model.dto.CloudAssetRequest;
import com.itam.model.dto.CloudAssetResponse;
import com.itam.model.dto.PageResponse;
import com.itam.model.entity.AssetTypeDefinition;
import com.itam.model.entity.CloudAsset;
import com.itam.model.entity.Person;
import com.itam.repository.AssetTypeDefinitionRepository;
import com.itam.repository.CloudAssetRepository;
import com.itam.repository.PersonRepository;
import com.itam.repository.spec.BaseAssetSpecifications;
import com.itam.repository.spec.CloudAssetSpecifications;
import com.itam.service.asset.CloudAssetService;
import com.itam.service.customfield.CustomFieldValidator;
import com.itam.validation.ValidationCollector;

@Service
@Transactional
public class CloudAssetServiceImpl implements CloudAssetService {

    private static final String CLOUD_TYPE_CODE = "CLOUD";

    private final CloudAssetRepository cloudAssetRepository;
    private final PersonRepository personRepository;
    private final AssetTypeDefinitionRepository assetTypeDefinitionRepository;
    private final CloudAssetMapper mapper;
    private final CustomFieldValidator customFieldValidator;

    public CloudAssetServiceImpl(CloudAssetRepository cloudAssetRepository,
            PersonRepository personRepository,
            AssetTypeDefinitionRepository assetTypeDefinitionRepository,
            CloudAssetMapper mapper,
            CustomFieldValidator customFieldValidator) {
        this.cloudAssetRepository = cloudAssetRepository;
        this.personRepository = personRepository;
        this.assetTypeDefinitionRepository = assetTypeDefinitionRepository;
        this.mapper = mapper;
        this.customFieldValidator = customFieldValidator;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CloudAssetResponse> list(CloudProvider provider, String region,
            AssetStatus status, Long responsibilityOwnerId, Pageable pageable) {
        Specification<CloudAsset> spec = Specification
                .where(CloudAssetSpecifications.notDeleted())
                .and(CloudAssetSpecifications.providerEquals(provider))
                .and(CloudAssetSpecifications.regionEquals(region))
                .and(CloudAssetSpecifications.statusEquals(status))
                .and(BaseAssetSpecifications.responsibilityOwnerEquals(responsibilityOwnerId));

        Page<CloudAsset> page = cloudAssetRepository.findAll(spec, pageable);
        return PageResponse.of(page, mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CloudAssetResponse get(Long id) {
        return mapper.toResponse(findActiveOrThrow(id));
    }

    @Override
    public CloudAssetResponse create(CloudAssetRequest request) {
        CloudAsset cloud = new CloudAsset();
        cloud.setAssetType(cloudAssetType());
        applyRequest(cloud, request);
        return mapper.toResponse(cloudAssetRepository.save(cloud));
    }

    @Override
    public CloudAssetResponse update(Long id, CloudAssetRequest request) {
        CloudAsset cloud = findActiveOrThrow(id);
        applyRequest(cloud, request);
        return mapper.toResponse(cloud);
    }

    private CloudAsset findActiveOrThrow(Long id) {
        return cloudAssetRepository.findById(id)
                .filter(cloud -> !cloud.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Cloud varlığı", id));
    }

    private void applyRequest(CloudAsset cloud, CloudAssetRequest request) {
        ValidationCollector errors = ValidationCollector.create();
        Person owner = resolveResponsibilityOwner(request.responsibilityOwnerId(), errors);
        customFieldValidator.validate(CLOUD_TYPE_CODE, request.customFields(), errors);
        errors.throwIfInvalid();

        cloud.setAssetName(request.assetName());
        cloud.setNotes(request.notes());
        cloud.setResponsibilityOwner(owner);
        cloud.setCloudProvider(request.cloudProvider());
        cloud.setInstanceType(request.instanceType());
        cloud.setRegion(request.region());
        cloud.setAccountId(request.accountId());
        cloud.setCustomFields(request.customFields() == null ? Map.of() : request.customFields());
    }

    private Person resolveResponsibilityOwner(Long id, ValidationCollector errors) {
        if (id == null) {
            return personRepository.findByCode(Person.COMPANY_CODE)
                    .orElseThrow(() -> new IllegalStateException(
                            "Sistem kaydı '%s' bulunamadı, V1 migration'ı kontrol et".formatted(Person.COMPANY_CODE)));
        }
        return personRepository.findByIdAndActiveTrue(id).orElseGet(() -> {
            errors.add("responsibilityOwnerId", "sorumluBulunamadi",
                    "Belirtilen sorumlu kişi bulunamadı", id);
            return null;
        });
    }

    private AssetTypeDefinition cloudAssetType() {
        return assetTypeDefinitionRepository.findById(CLOUD_TYPE_CODE)
                .orElseThrow(() -> new IllegalStateException(
                        "Sistem asset tipi '%s' bulunamadı, V1 migration'ı kontrol et".formatted(CLOUD_TYPE_CODE)));
    }
}
