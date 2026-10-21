package com.itam.mapper;

import org.springframework.stereotype.Component;

import com.itam.model.dto.CloudAssetResponse;
import com.itam.model.dto.PersonSummary;
import com.itam.model.entity.CloudAsset;
import com.itam.model.entity.Person;

@Component
public class CloudAssetMapper {

    public CloudAssetResponse toResponse(CloudAsset cloud) {
        return new CloudAssetResponse(
                cloud.getId(),
                cloud.getAssetName(),
                cloud.getStatus(),
                cloud.getNotes(),
                toSummary(cloud.getResponsibilityOwner()),
                cloud.getCloudProvider(),
                cloud.getInstanceType(),
                cloud.getRegion(),
                cloud.getAccountId(),
                cloud.getCustomFields(),
                cloud.getCreatedAt(),
                cloud.getUpdatedAt());
    }

    private PersonSummary toSummary(Person person) {
        return person == null ? null : new PersonSummary(person.getId(), person.getFullName());
    }
}
