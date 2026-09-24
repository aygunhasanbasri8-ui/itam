package tr.com.nkolay.itam.mapper;

import org.springframework.stereotype.Component;

import tr.com.nkolay.itam.model.dto.ApplicationAssetResponse;
import tr.com.nkolay.itam.model.dto.PersonSummary;
import tr.com.nkolay.itam.model.entity.ApplicationAsset;
import tr.com.nkolay.itam.model.entity.Person;

@Component
public class ApplicationAssetMapper {

    public ApplicationAssetResponse toResponse(ApplicationAsset application) {
        return new ApplicationAssetResponse(
                application.getId(),
                application.getAssetName(),
                application.getStatus(),
                application.getNotes(),
                toSummary(application.getResponsibilityOwner()),
                application.getVersion(),
                application.getLicenseType(),
                application.getLicenseCount(),
                application.getVendor(),
                application.getExpiryDate(),
                application.getInstallPath(),
                application.getCreatedAt(),
                application.getUpdatedAt());
    }

    private PersonSummary toSummary(Person person) {
        return person == null ? null : new PersonSummary(person.getId(), person.getFullName());
    }
}
