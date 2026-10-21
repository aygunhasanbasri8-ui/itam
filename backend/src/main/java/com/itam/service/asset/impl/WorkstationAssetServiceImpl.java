package com.itam.service.asset.impl;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itam.enums.AssetStatus;
import com.itam.exception.ResourceNotFoundException;
import com.itam.mapper.WorkstationAssetMapper;
import com.itam.model.dto.PageResponse;
import com.itam.model.dto.WorkstationAssetRequest;
import com.itam.model.dto.WorkstationAssetResponse;
import com.itam.model.entity.AssetTypeDefinition;
import com.itam.model.entity.Department;
import com.itam.model.entity.Person;
import com.itam.model.entity.WorkstationAsset;
import com.itam.repository.AssetTypeDefinitionRepository;
import com.itam.repository.DepartmentRepository;
import com.itam.repository.PersonRepository;
import com.itam.repository.WorkstationAssetRepository;
import com.itam.repository.spec.BaseAssetSpecifications;
import com.itam.repository.spec.WorkstationAssetSpecifications;
import com.itam.service.asset.WorkstationAssetService;
import com.itam.service.customfield.CustomFieldValidator;
import com.itam.validation.ValidationCollector;

@Service
@Transactional
public class WorkstationAssetServiceImpl implements WorkstationAssetService {

    private static final String WORKSTATION_TYPE_CODE = "WORKSTATION";

    private final WorkstationAssetRepository workstationAssetRepository;
    private final PersonRepository personRepository;
    private final DepartmentRepository departmentRepository;
    private final AssetTypeDefinitionRepository assetTypeDefinitionRepository;
    private final WorkstationAssetMapper mapper;
    private final CustomFieldValidator customFieldValidator;

    public WorkstationAssetServiceImpl(WorkstationAssetRepository workstationAssetRepository,
            PersonRepository personRepository,
            DepartmentRepository departmentRepository,
            AssetTypeDefinitionRepository assetTypeDefinitionRepository,
            WorkstationAssetMapper mapper,
            CustomFieldValidator customFieldValidator) {
        this.workstationAssetRepository = workstationAssetRepository;
        this.personRepository = personRepository;
        this.departmentRepository = departmentRepository;
        this.assetTypeDefinitionRepository = assetTypeDefinitionRepository;
        this.mapper = mapper;
        this.customFieldValidator = customFieldValidator;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorkstationAssetResponse> list(String serialNumber, AssetStatus status,
            Long responsibilityOwnerId,
            Long departmentId, Long assignedPersonId, Pageable pageable) {
        Specification<WorkstationAsset> spec = Specification
                .where(WorkstationAssetSpecifications.notDeleted())
                .and(WorkstationAssetSpecifications.serialNumberContains(serialNumber))
                .and(WorkstationAssetSpecifications.statusEquals(status))
                .and(BaseAssetSpecifications.responsibilityOwnerEquals(responsibilityOwnerId))
                .and(WorkstationAssetSpecifications.departmentEquals(departmentId))
                .and(WorkstationAssetSpecifications.assignedPersonEquals(assignedPersonId));

        Page<WorkstationAsset> page = workstationAssetRepository.findAll(spec, pageable);
        return PageResponse.of(page, mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkstationAssetResponse get(Long id) {
        return mapper.toResponse(findActiveOrThrow(id));
    }

    @Override
    public WorkstationAssetResponse create(WorkstationAssetRequest request) {
        WorkstationAsset workstation = new WorkstationAsset();
        workstation.setAssetType(workstationAssetType());
        applyRequest(workstation, request);
        return mapper.toResponse(workstationAssetRepository.save(workstation));
    }

    @Override
    public WorkstationAssetResponse update(Long id, WorkstationAssetRequest request) {
        WorkstationAsset workstation = findActiveOrThrow(id);
        applyRequest(workstation, request);
        return mapper.toResponse(workstation);
    }

    private WorkstationAsset findActiveOrThrow(Long id) {
        return workstationAssetRepository.findById(id)
                .filter(workstation -> !workstation.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("İş istasyonu", id));
    }

    private void applyRequest(WorkstationAsset workstation, WorkstationAssetRequest request) {
        ValidationCollector errors = ValidationCollector.create();

        Person owner = resolveResponsibilityOwner(request.responsibilityOwnerId(), errors);
        Person assignedPerson = resolvePerson(request.assignedPersonId(), "assignedPersonId", errors);
        Department department = resolveDepartment(request.departmentId(), errors);
        customFieldValidator.validate(WORKSTATION_TYPE_CODE, request.customFields(), errors);

        errors.throwIfInvalid();

        workstation.setAssetName(request.assetName());
        workstation.setNotes(request.notes());
        workstation.setResponsibilityOwner(owner);
        workstation.setSerialNumber(request.serialNumber());
        workstation.setModel(request.model());
        workstation.setCpu(request.cpu());
        workstation.setRamGb(request.ramGb());
        workstation.setDiskGb(request.diskGb());
        workstation.setAssignedPerson(assignedPerson);
        workstation.setDepartment(department);
        workstation.setCustomFields(request.customFields() == null ? Map.of() : request.customFields());
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

    private Department resolveDepartment(Long id, ValidationCollector errors) {
        if (id == null) {
            return null;
        }
        return departmentRepository.findByIdAndActiveTrue(id).orElseGet(() -> {
            errors.add("departmentId", "departmanBulunamadi", "Belirtilen departman bulunamadı", id);
            return null;
        });
    }

    private AssetTypeDefinition workstationAssetType() {
        return assetTypeDefinitionRepository.findById(WORKSTATION_TYPE_CODE)
                .orElseThrow(() -> new IllegalStateException(
                        "Sistem asset tipi '%s' bulunamadı, V1 migration'ı kontrol et".formatted(WORKSTATION_TYPE_CODE)));
    }
}
