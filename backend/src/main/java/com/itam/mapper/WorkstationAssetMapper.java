package com.itam.mapper;

import org.springframework.stereotype.Component;

import com.itam.model.dto.DepartmentSummary;
import com.itam.model.dto.PersonSummary;
import com.itam.model.dto.WorkstationAssetResponse;
import com.itam.model.entity.Department;
import com.itam.model.entity.Person;
import com.itam.model.entity.WorkstationAsset;

@Component
public class WorkstationAssetMapper {

    public WorkstationAssetResponse toResponse(WorkstationAsset workstation) {
        return new WorkstationAssetResponse(
                workstation.getId(),
                workstation.getAssetName(),
                workstation.getStatus(),
                workstation.getNotes(),
                toPersonSummary(workstation.getResponsibilityOwner()),
                workstation.getSerialNumber(),
                workstation.getModel(),
                workstation.getCpu(),
                workstation.getRamGb(),
                workstation.getDiskGb(),
                toPersonSummary(workstation.getAssignedPerson()),
                toDepartmentSummary(workstation.getDepartment()),
                workstation.getCustomFields(),
                workstation.getCreatedAt(),
                workstation.getUpdatedAt());
    }

    private PersonSummary toPersonSummary(Person person) {
        return person == null ? null : new PersonSummary(person.getId(), person.getFullName());
    }

    private DepartmentSummary toDepartmentSummary(Department department) {
        return department == null ? null : new DepartmentSummary(department.getId(), department.getName());
    }
}
