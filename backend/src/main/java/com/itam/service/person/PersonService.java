package com.itam.service.person;

import org.springframework.data.domain.Pageable;

import com.itam.model.dto.PageResponse;
import com.itam.model.dto.PersonRequest;
import com.itam.model.dto.PersonResponse;

public interface PersonService {

    PageResponse<PersonResponse> list(String search, Boolean active, Long departmentId, Pageable pageable);

    PersonResponse get(Long id);

    PersonResponse create(PersonRequest request);

    PersonResponse update(Long id, PersonRequest request);
}
