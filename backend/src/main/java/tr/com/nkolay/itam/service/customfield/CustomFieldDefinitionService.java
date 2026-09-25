package tr.com.nkolay.itam.service.customfield;

import java.util.List;

import tr.com.nkolay.itam.model.dto.CustomFieldDefinitionRequest;
import tr.com.nkolay.itam.model.dto.CustomFieldDefinitionResponse;
import tr.com.nkolay.itam.model.dto.CustomFieldDefinitionUpdateRequest;

public interface CustomFieldDefinitionService {

    List<CustomFieldDefinitionResponse> listByAssetType(String assetTypeCode);

    CustomFieldDefinitionResponse create(CustomFieldDefinitionRequest request);

    CustomFieldDefinitionResponse update(Long id, CustomFieldDefinitionUpdateRequest request);
}
