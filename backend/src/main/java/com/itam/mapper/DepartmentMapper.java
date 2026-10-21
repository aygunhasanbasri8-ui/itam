package com.itam.mapper;

import org.springframework.stereotype.Component;

import com.itam.model.dto.DepartmentResponse;
import com.itam.model.entity.Department;

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
