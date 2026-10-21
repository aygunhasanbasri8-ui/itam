package com.itam.service.department;

import org.springframework.data.domain.Pageable;

import com.itam.model.dto.DepartmentRequest;
import com.itam.model.dto.DepartmentResponse;
import com.itam.model.dto.PageResponse;

public interface DepartmentService {

    PageResponse<DepartmentResponse> list(String search, Boolean active, Pageable pageable);

    DepartmentResponse get(Long id);

    DepartmentResponse create(DepartmentRequest request);

    DepartmentResponse update(Long id, DepartmentRequest request);
}
