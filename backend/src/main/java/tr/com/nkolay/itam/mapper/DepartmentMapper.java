package tr.com.nkolay.itam.mapper;

import org.springframework.stereotype.Component;

import tr.com.nkolay.itam.model.dto.DepartmentResponse;
import tr.com.nkolay.itam.model.entity.Department;

@Component
public class DepartmentMapper {

    public DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.isActive(),
                department.getCreatedAt(),
                department.getUpdatedAt());
    }
}
