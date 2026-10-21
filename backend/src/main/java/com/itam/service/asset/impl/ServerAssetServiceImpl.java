package com.itam.service.asset.impl;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itam.enums.AssetStatus;
import com.itam.enums.ServerEnvironment;
import com.itam.exception.ResourceNotFoundException;
import com.itam.mapper.ServerAssetMapper;
import com.itam.model.dto.PageResponse;
import com.itam.model.dto.ServerAssetRequest;
import com.itam.model.dto.ServerAssetResponse;
import com.itam.model.entity.AssetTypeDefinition;
import com.itam.model.entity.Person;
import com.itam.model.entity.ServerAsset;
import com.itam.repository.AssetTypeDefinitionRepository;
import com.itam.repository.PersonRepository;
import com.itam.repository.ServerAssetRepository;
import com.itam.repository.spec.BaseAssetSpecifications;
import com.itam.repository.spec.ServerAssetSpecifications;
import com.itam.service.asset.ServerAssetService;
import com.itam.service.customfield.CustomFieldValidator;
import com.itam.validation.ValidationCollector;

@Service
@Transactional
public class ServerAssetServiceImpl implements ServerAssetService {

    /** asset_type_def'te V1 migration ile hazır gelen sistem kaydı, hiçbir zaman silinmez. */
    private static final String SERVER_TYPE_CODE = "SERVER";

    private final ServerAssetRepository serverAssetRepository;
    private final PersonRepository personRepository;
    private final AssetTypeDefinitionRepository assetTypeDefinitionRepository;
    private final ServerAssetMapper mapper;
    private final CustomFieldValidator customFieldValidator;

    public ServerAssetServiceImpl(ServerAssetRepository serverAssetRepository,
            PersonRepository personRepository,
            AssetTypeDefinitionRepository assetTypeDefinitionRepository,
            ServerAssetMapper mapper,
            CustomFieldValidator customFieldValidator) {
        this.serverAssetRepository = serverAssetRepository;
        this.personRepository = personRepository;
        this.assetTypeDefinitionRepository = assetTypeDefinitionRepository;
        this.mapper = mapper;
        this.customFieldValidator = customFieldValidator;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ServerAssetResponse> list(String hostname, ServerEnvironment environment,
            AssetStatus status, Long responsibilityOwnerId, Pageable pageable) {
        Specification<ServerAsset> spec = Specification
                .where(ServerAssetSpecifications.notDeleted())
                .and(ServerAssetSpecifications.hostnameContains(hostname))
                .and(ServerAssetSpecifications.environmentEquals(environment))
                .and(ServerAssetSpecifications.statusEquals(status))
                .and(BaseAssetSpecifications.responsibilityOwnerEquals(responsibilityOwnerId));

        Page<ServerAsset> page = serverAssetRepository.findAll(spec, pageable);
        return PageResponse.of(page, mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ServerAssetResponse get(Long id) {
        return mapper.toResponse(findActiveOrThrow(id));
    }

    @Override
    public ServerAssetResponse create(ServerAssetRequest request) {
        ServerAsset server = new ServerAsset();
        server.setAssetType(serverAssetType());
        applyRequest(server, request);
        return mapper.toResponse(serverAssetRepository.save(server));
    }

    @Override
    public ServerAssetResponse update(Long id, ServerAssetRequest request) {
        ServerAsset server = findActiveOrThrow(id);
        applyRequest(server, request);
        return mapper.toResponse(server);
    }

    private ServerAsset findActiveOrThrow(Long id) {
        return serverAssetRepository.findById(id)
                .filter(server -> !server.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Sunucu", id));
    }

    /**
     * Sorumlu kişi alanları burada tek seferde doğrulanıp entity'e yazılır.
     * Hepsi aynı anda kontrol edilir ki kullanıcı üç ayrı hatayı tek tek değil
     * hepsini birden görsün.
     */
    private void applyRequest(ServerAsset server, ServerAssetRequest request) {
        ValidationCollector errors = ValidationCollector.create();

        Person owner = resolveResponsibilityOwner(request.responsibilityOwnerId(), errors);
        Person serviceOwner = resolvePerson(request.serviceOwnerId(), "serviceOwnerId", errors);
        Person appResponsible = resolvePerson(request.appResponsibleId(), "appResponsibleId", errors);
        Person osResponsible = resolvePerson(request.osResponsibleId(), "osResponsibleId", errors);
        customFieldValidator.validate(SERVER_TYPE_CODE, request.customFields(), errors);

        errors.throwIfInvalid();

        server.setAssetName(request.assetName());
        server.setNotes(request.notes());
        server.setResponsibilityOwner(owner);
        server.setDomain(request.domain());
        server.setHostname(request.hostname());
        server.setIpAddress(request.ipAddress());
        server.setEnvironment(request.environment());
        server.setServerClass(request.serverClass());
        server.setGroupName(request.groupName());
        server.setGroupInfo(request.groupInfo());
        server.setGroupDesc(request.groupDesc());
        server.setInfrastructureType(request.infrastructureType());
        server.setOs(request.os());
        server.setVirtual(request.virtual());
        server.setServiceOwner(serviceOwner);
        server.setAppResponsible(appResponsible);
        server.setOsResponsible(osResponsible);
        server.setCustomFields(request.customFields() == null ? Map.of() : request.customFields());
    }

    /** responsibilityOwnerId boş geçilirse FIRMA sistem kaydı atanır — bu alan NOT NULL. */
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

    private AssetTypeDefinition serverAssetType() {
        return assetTypeDefinitionRepository.findById(SERVER_TYPE_CODE)
                .orElseThrow(() -> new IllegalStateException(
                        "Sistem asset tipi '%s' bulunamadı, V1 migration'ı kontrol et".formatted(SERVER_TYPE_CODE)));
    }
}
