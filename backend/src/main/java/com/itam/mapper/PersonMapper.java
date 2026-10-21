package com.itam.mapper;

import org.springframework.stereotype.Component;

import com.itam.model.dto.DepartmentSummary;
import com.itam.model.dto.PersonResponse;
import com.itam.model.entity.Department;
import com.itam.model.entity.Person;

@Component
public class PersonMapper {

    public PersonResponse toResponse(Person person) {
        return new PersonResponse(
                person.getId(),
                person.getFullName(),
                person.getCode(),
                person.getEmail(),
                person.getTitle(),
                person.getPhone(),
                toSummary(person.getDepartment()),
                person.isSystem(),
                person.isActive(),
                person.getCreatedAt(),
                person.getUpdatedAt());
    }

    private DepartmentSummary toSummary(Department department) {
        return department == null ? null : new DepartmentSummary(department.getId(), department.getName());
    }
}
