package tr.com.nkolay.itam.service.asset.impl;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.enums.CloudProvider;
import tr.com.nkolay.itam.exception.ResourceNotFoundException;
import tr.com.nkolay.itam.mapper.CloudAssetMapper;
import tr.com.nkolay.itam.model.dto.CloudAssetRequest;
import tr.com.nkolay.itam.model.dto.CloudAssetResponse;
import tr.com.nkolay.itam.model.dto.PageResponse;
import tr.com.nkolay.itam.model.entity.AssetTypeDefinition;
import tr.com.nkolay.itam.model.entity.CloudAsset;
import tr.com.nkolay.itam.model.entity.Person;
import tr.com.nkolay.itam.repository.AssetTypeDefinitionRepository;
import tr.com.nkolay.itam.repository.CloudAssetRepository;
import tr.com.nkolay.itam.repository.PersonRepository;
import tr.com.nkolay.itam.repository.spec.CloudAssetSpecifications;
import tr.com.nkolay.itam.service.asset.CloudAssetService;
import tr.com.nkolay.itam.service.customfield.CustomFieldValidator;
import tr.com.nkolay.itam.validation.ValidationCollector;

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
            AssetStatus status, Pageable pageable) {
        Specification<CloudAsset> spec = Specification
                .where(CloudAssetSpecifications.notDeleted())
                .and(CloudAssetSpecifications.providerEquals(provider))
                .and(CloudAssetSpecifications.regionEquals(region))
                .and(CloudAssetSpecifications.statusEquals(status));

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
            errors.add("responsibilityOwnerId", "sorumluBulunamadi", "Belirtilen sorumlu kişi bulunamadı", id);
            return null;
        });
    }

    private AssetTypeDefinition cloudAssetType() {
        return assetTypeDefinitionRepository.findById(CLOUD_TYPE_CODE)
                .orElseThrow(() -> new IllegalStateException(
                        "Sistem asset tipi '%s' bulunamadı, V1 migration'ı kontrol et".formatted(CLOUD_TYPE_CODE)));
    }
}
