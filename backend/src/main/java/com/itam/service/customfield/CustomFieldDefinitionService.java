package com.itam.service.customfield;

import java.util.List;

import com.itam.model.dto.CustomFieldDefinitionRequest;
import com.itam.model.dto.CustomFieldDefinitionResponse;
import com.itam.model.dto.CustomFieldDefinitionUpdateRequest;

public interface CustomFieldDefinitionService {

    List<CustomFieldDefinitionResponse> listByAssetType(String assetTypeCode);

    CustomFieldDefinitionResponse create(CustomFieldDefinitionRequest request);

    CustomFieldDefinitionResponse update(Long id, CustomFieldDefinitionUpdateRequest request);
}
