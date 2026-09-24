package tr.com.nkolay.itam.mapper;

import org.springframework.stereotype.Component;

import tr.com.nkolay.itam.model.dto.DepartmentSummary;
import tr.com.nkolay.itam.model.dto.PersonResponse;
import tr.com.nkolay.itam.model.entity.Department;
import tr.com.nkolay.itam.model.entity.Person;

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
