package com.itam.mapper;

import org.springframework.stereotype.Component;

import com.itam.model.dto.BusinessServiceAssetResponse;
import com.itam.model.dto.PersonSummary;
import com.itam.model.entity.BusinessServiceAsset;
import com.itam.model.entity.Person;

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
                service.getCustomFields(),
                service.getCreatedAt(),
                service.getUpdatedAt());
    }

    private PersonSummary toSummary(Person person) {
        return person == null ? null : new PersonSummary(person.getId(), person.getFullName());
    }
}
