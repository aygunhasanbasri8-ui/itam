package tr.com.nkolay.itam.mapper;

import org.springframework.stereotype.Component;

import tr.com.nkolay.itam.model.dto.AssetSummaryResponse;
import tr.com.nkolay.itam.model.dto.PersonSummary;
import tr.com.nkolay.itam.model.entity.BaseAsset;
import tr.com.nkolay.itam.model.entity.Person;

@Component
public class AssetSummaryMapper {

    public AssetSummaryResponse toSummary(BaseAsset asset) {
        return new AssetSummaryResponse(
                asset.getId(),
                asset.getAssetName(),
                asset.getAssetType().getCode(),
                asset.getStatus(),
                toSummary(asset.getResponsibilityOwner()),
                asset.getCreatedAt());
    }

    private PersonSummary toSummary(Person person) {
        return person == null ? null : new PersonSummary(person.getId(), person.getFullName());
    }
}
