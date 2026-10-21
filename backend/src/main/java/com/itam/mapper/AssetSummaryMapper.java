package com.itam.mapper;

import org.springframework.stereotype.Component;

import com.itam.model.dto.AssetSummaryResponse;
import com.itam.model.dto.PersonSummary;
import com.itam.model.entity.BaseAsset;
import com.itam.model.entity.Person;

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
