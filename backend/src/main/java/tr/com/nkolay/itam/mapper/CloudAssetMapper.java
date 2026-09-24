package tr.com.nkolay.itam.mapper;

import org.springframework.stereotype.Component;

import tr.com.nkolay.itam.model.dto.CloudAssetResponse;
import tr.com.nkolay.itam.model.dto.PersonSummary;
import tr.com.nkolay.itam.model.entity.CloudAsset;
import tr.com.nkolay.itam.model.entity.Person;

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
                cloud.getCreatedAt(),
                cloud.getUpdatedAt());
    }

    private PersonSummary toSummary(Person person) {
        return person == null ? null : new PersonSummary(person.getId(), person.getFullName());
    }
}
