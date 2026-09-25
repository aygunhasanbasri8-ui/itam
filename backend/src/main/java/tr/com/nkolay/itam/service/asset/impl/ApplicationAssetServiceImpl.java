package tr.com.nkolay.itam.service.asset.impl;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.enums.AssetStatus;
import tr.com.nkolay.itam.exception.ResourceNotFoundException;
import tr.com.nkolay.itam.mapper.ApplicationAssetMapper;
import tr.com.nkolay.itam.model.dto.ApplicationAssetRequest;
import tr.com.nkolay.itam.model.dto.ApplicationAssetResponse;
import tr.com.nkolay.itam.model.dto.PageResponse;
import tr.com.nkolay.itam.model.entity.ApplicationAsset;
import tr.com.nkolay.itam.model.entity.AssetTypeDefinition;
import tr.com.nkolay.itam.model.entity.Person;
import tr.com.nkolay.itam.repository.ApplicationAssetRepository;
import tr.com.nkolay.itam.repository.AssetTypeDefinitionRepository;
import tr.com.nkolay.itam.repository.PersonRepository;
import tr.com.nkolay.itam.repository.spec.ApplicationAssetSpecifications;
import tr.com.nkolay.itam.service.asset.ApplicationAssetService;
import tr.com.nkolay.itam.service.customfield.CustomFieldValidator;
import tr.com.nkolay.itam.validation.ValidationCollector;

@Service
@Transactional
public class ApplicationAssetServiceImpl implements ApplicationAssetService {

    private static final String APPLICATION_TYPE_CODE = "APPLICATION";

    private final ApplicationAssetRepository applicationAssetRepository;
    private final PersonRepository personRepository;
    private final AssetTypeDefinitionRepository assetTypeDefinitionRepository;
    private final ApplicationAssetMapper mapper;
    private final CustomFieldValidator customFieldValidator;

    public ApplicationAssetServiceImpl(ApplicationAssetRepository applicationAssetRepository,
            PersonRepository personRepository,
            AssetTypeDefinitionRepository assetTypeDefinitionRepository,
            ApplicationAssetMapper mapper,
            CustomFieldValidator customFieldValidator) {
        this.applicationAssetRepository = applicationAssetRepository;
        this.personRepository = personRepository;
        this.assetTypeDefinitionRepository = assetTypeDefinitionRepository;
        this.mapper = mapper;
        this.customFieldValidator = customFieldValidator;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ApplicationAssetResponse> list(String vendor, AssetStatus status,
            LocalDate expiryBefore, Pageable pageable) {
        Specification<ApplicationAsset> spec = Specification
                .where(ApplicationAssetSpecifications.notDeleted())
                .and(ApplicationAssetSpecifications.vendorContains(vendor))
                .and(ApplicationAssetSpecifications.statusEquals(status))
                .and(ApplicationAssetSpecifications.expiryBefore(expiryBefore));

        Page<ApplicationAsset> page = applicationAssetRepository.findAll(spec, pageable);
        return PageResponse.of(page, mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationAssetResponse get(Long id) {
        return mapper.toResponse(findActiveOrThrow(id));
    }

    @Override
    public ApplicationAssetResponse create(ApplicationAssetRequest request) {
        ApplicationAsset application = new ApplicationAsset();
        application.setAssetType(applicationAssetType());
        applyRequest(application, request);
        return mapper.toResponse(applicationAssetRepository.save(application));
    }

    @Override
    public ApplicationAssetResponse update(Long id, ApplicationAssetRequest request) {
        ApplicationAsset application = findActiveOrThrow(id);
        applyRequest(application, request);
        return mapper.toResponse(application);
    }

    private ApplicationAsset findActiveOrThrow(Long id) {
        return applicationAssetRepository.findById(id)
                .filter(application -> !application.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Uygulama/yazılım", id));
    }

    private void applyRequest(ApplicationAsset application, ApplicationAssetRequest request) {
        ValidationCollector errors = ValidationCollector.create();
        Person owner = resolveResponsibilityOwner(request.responsibilityOwnerId(), errors);
        customFieldValidator.validate(APPLICATION_TYPE_CODE, request.customFields(), errors);
        errors.throwIfInvalid();

        application.setAssetName(request.assetName());
        application.setNotes(request.notes());
        application.setResponsibilityOwner(owner);
        application.setVersion(request.version());
        application.setLicenseType(request.licenseType());
        application.setLicenseCount(request.licenseCount());
        application.setVendor(request.vendor());
        application.setExpiryDate(request.expiryDate());
        application.setInstallPath(request.installPath());
        application.setCustomFields(request.customFields() == null ? Map.of() : request.customFields());
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

    private AssetTypeDefinition applicationAssetType() {
        return assetTypeDefinitionRepository.findById(APPLICATION_TYPE_CODE)
                .orElseThrow(() -> new IllegalStateException(
                        "Sistem asset tipi '%s' bulunamadı, V1 migration'ı kontrol et".formatted(APPLICATION_TYPE_CODE)));
    }
}
