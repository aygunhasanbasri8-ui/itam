package tr.com.nkolay.itam.mapper;

import org.springframework.stereotype.Component;

import tr.com.nkolay.itam.model.dto.BusinessServiceAssetResponse;
import tr.com.nkolay.itam.model.dto.PersonSummary;
import tr.com.nkolay.itam.model.entity.BusinessServiceAsset;
import tr.com.nkolay.itam.model.entity.Person;

@Component
public class BusinessServiceAssetMapper {

    public BusinessServiceAssetResponse toResponse(BusinessServiceAsset service) {
        return new BusinessServiceAssetResponse(
                service.getId(),
                service.getAssetName(),
                service.getStatus(),
                service.getNotes(),
                toSummary(service.getResponsibilityOwner()),
                service.getCriticality(),
                service.getSla(),
                toSummary(service.getServiceOwner()),
                service.getCreatedAt(),
                service.getUpdatedAt());
    }

    private PersonSummary toSummary(Person person) {
        return person == null ? null : new PersonSummary(person.getId(), person.getFullName());
    }
}
