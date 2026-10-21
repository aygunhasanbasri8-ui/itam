package com.itam.mapper;

import org.springframework.stereotype.Component;

import com.itam.model.dto.ApplicationAssetResponse;
import com.itam.model.dto.PersonSummary;
import com.itam.model.entity.ApplicationAsset;
import com.itam.model.entity.Person;

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
                application.getCustomFields(),
                application.getCreatedAt(),
                application.getUpdatedAt());
    }

    private PersonSummary toSummary(Person person) {
        return person == null ? null : new PersonSummary(person.getId(), person.getFullName());
    }
}
