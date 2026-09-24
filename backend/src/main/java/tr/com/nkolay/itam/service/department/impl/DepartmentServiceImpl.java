package tr.com.nkolay.itam.service.department.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.exception.ResourceNotFoundException;
import tr.com.nkolay.itam.mapper.DepartmentMapper;
import tr.com.nkolay.itam.model.dto.DepartmentRequest;
import tr.com.nkolay.itam.model.dto.DepartmentResponse;
import tr.com.nkolay.itam.model.dto.PageResponse;
import tr.com.nkolay.itam.model.entity.Department;
import tr.com.nkolay.itam.repository.DepartmentRepository;
import tr.com.nkolay.itam.repository.spec.DepartmentSpecifications;
import tr.com.nkolay.itam.service.department.DepartmentService;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper mapper;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, DepartmentMapper mapper) {
        this.departmentRepository = departmentRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> list(String search, Boolean active, Pageable pageable) {
        Specification<Department> spec = Specification
                .where(DepartmentSpecifications.nameOrCodeContains(search))
                .and(DepartmentSpecifications.activeEquals(active));

        Page<Department> page = departmentRepository.findAll(spec, pageable);
        return PageResponse.of(page, mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse get(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    public DepartmentResponse create(DepartmentRequest request) {
        Department department = new Department();
        applyRequest(department, request);
        return mapper.toResponse(departmentRepository.save(department));
    }

    @Override
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = findOrThrow(id);
        applyRequest(department, request);
        return mapper.toResponse(department);
    }

    private Department findOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departman", id));
    }

    private void applyRequest(Department department, DepartmentRequest request) {
        department.setName(request.name());
        department.setCode(request.code());
        department.setActive(request.active());
    }
}
