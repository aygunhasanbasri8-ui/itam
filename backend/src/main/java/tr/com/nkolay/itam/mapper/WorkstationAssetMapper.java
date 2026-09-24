package tr.com.nkolay.itam.mapper;

import org.springframework.stereotype.Component;

import tr.com.nkolay.itam.model.dto.DepartmentSummary;
import tr.com.nkolay.itam.model.dto.PersonSummary;
import tr.com.nkolay.itam.model.dto.WorkstationAssetResponse;
import tr.com.nkolay.itam.model.entity.Department;
import tr.com.nkolay.itam.model.entity.Person;
import tr.com.nkolay.itam.model.entity.WorkstationAsset;

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
