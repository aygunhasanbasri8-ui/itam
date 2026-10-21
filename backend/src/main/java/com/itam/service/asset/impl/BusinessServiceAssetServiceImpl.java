package com.itam.service.asset.impl;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itam.enums.AssetStatus;
import com.itam.enums.ServiceCriticality;
import com.itam.exception.ResourceNotFoundException;
import com.itam.mapper.BusinessServiceAssetMapper;
import com.itam.model.dto.BusinessServiceAssetRequest;
import com.itam.model.dto.BusinessServiceAssetResponse;
import com.itam.model.dto.PageResponse;
import com.itam.model.entity.AssetTypeDefinition;
import com.itam.model.entity.BusinessServiceAsset;
import com.itam.model.entity.Person;
import com.itam.repository.AssetTypeDefinitionRepository;
import com.itam.repository.BusinessServiceAssetRepository;
import com.itam.repository.PersonRepository;
import com.itam.repository.spec.BaseAssetSpecifications;
import com.itam.repository.spec.BusinessServiceAssetSpecifications;
import com.itam.service.asset.BusinessServiceAssetService;
import com.itam.service.customfield.CustomFieldValidator;
import com.itam.validation.ValidationCollector;

@Service
@Transactional
public class BusinessServiceAssetServiceImpl implements BusinessServiceAssetService {

    private static final String BUSINESS_SERVICE_TYPE_CODE = "BUSINESS_SERVICE";

    private final BusinessServiceAssetRepository businessServiceAssetRepository;
    private final PersonRepository personRepository;
    private final AssetTypeDefinitionRepository assetTypeDefinitionRepository;
    private final BusinessServiceAssetMapper mapper;
    private final CustomFieldValidator customFieldValidator;

    public BusinessServiceAssetServiceImpl(BusinessServiceAssetRepository businessServiceAssetRepository,
            PersonRepository personRepository,
            AssetTypeDefinitionRepository assetTypeDefinitionRepository,
            BusinessServiceAssetMapper mapper,
            CustomFieldValidator customFieldValidator) {
        this.businessServiceAssetRepository = businessServiceAssetRepository;
        this.personRepository = personRepository;
        this.assetTypeDefinitionRepository = assetTypeDefinitionRepository;
        this.mapper = mapper;
        this.customFieldValidator = customFieldValidator;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BusinessServiceAssetResponse> list(ServiceCriticality criticality,
            AssetStatus status, Long responsibilityOwnerId, Pageable pageable) {
        Specification<BusinessServiceAsset> spec = Specification
                .where(BusinessServiceAssetSpecifications.notDeleted())
                .and(BusinessServiceAssetSpecifications.criticalityEquals(criticality))
                .and(BusinessServiceAssetSpecifications.statusEquals(status))
                .and(BaseAssetSpecifications.responsibilityOwnerEquals(responsibilityOwnerId));

        Page<BusinessServiceAsset> page = businessServiceAssetRepository.findAll(spec, pageable);
        return PageResponse.of(page, mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessServiceAssetResponse get(Long id) {
        return mapper.toResponse(findActiveOrThrow(id));
    }

    @Override
    public BusinessServiceAssetResponse create(BusinessServiceAssetRequest request) {
        BusinessServiceAsset service = new BusinessServiceAsset();
        service.setAssetType(businessServiceAssetType());
        applyRequest(service, request);
        return mapper.toResponse(businessServiceAssetRepository.save(service));
    }

    @Override
    public BusinessServiceAssetResponse update(Long id, BusinessServiceAssetRequest request) {
        BusinessServiceAsset service = findActiveOrThrow(id);
        applyRequest(service, request);
        return mapper.toResponse(service);
    }

    private BusinessServiceAsset findActiveOrThrow(Long id) {
        return businessServiceAssetRepository.findById(id)
                .filter(service -> !service.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("İş servisi", id));
    }

    private void applyRequest(BusinessServiceAsset service, BusinessServiceAssetRequest request) {
        ValidationCollector errors = ValidationCollector.create();
        Person owner = resolveResponsibilityOwner(request.responsibilityOwnerId(), errors);
        Person serviceOwner = resolvePerson(request.serviceOwnerId(), "serviceOwnerId", errors);
        customFieldValidator.validate(BUSINESS_SERVICE_TYPE_CODE, request.customFields(), errors);
        errors.throwIfInvalid();

        service.setAssetName(request.assetName());
        service.setNotes(request.notes());
        service.setResponsibilityOwner(owner);
        service.setCriticality(request.criticality() == null ? ServiceCriticality.MID : request.criticality());
        service.setSla(request.sla());
        service.setServiceOwner(serviceOwner);
        service.setCustomFields(request.customFields() == null ? Map.of() : request.customFields());
    }

    private Person resolveResponsibilityOwner(Long id, ValidationCollector errors) {
        if (id == null) {
            return personRepository.findByCode(Person.COMPANY_CODE)
                    .orElseThrow(() -> new IllegalStateException(
                            "Sistem kaydı '%s' bulunamadı, V1 migration'ı kontrol et".formatted(Person.COMPANY_CODE)));
        }
        return resolvePerson(id, "responsibilityOwnerId", errors);
    }

    private Person resolvePerson(Long id, String field, ValidationCollector errors) {
        if (id == null) {
            return null;
        }
        return personRepository.findByIdAndActiveTrue(id).orElseGet(() -> {
            errors.add(field, "sorumluBulunamadi", "Belirtilen sorumlu kişi bulunamadı", id);
            return null;
        });
    }

    private AssetTypeDefinition businessServiceAssetType() {
        return assetTypeDefinitionRepository.findById(BUSINESS_SERVICE_TYPE_CODE)
                .orElseThrow(() -> new IllegalStateException(
                        "Sistem asset tipi '%s' bulunamadı, V1 migration'ı kontrol et"
                                .formatted(BUSINESS_SERVICE_TYPE_CODE)));
    }
}
