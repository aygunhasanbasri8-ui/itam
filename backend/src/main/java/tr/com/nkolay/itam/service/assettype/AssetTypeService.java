package tr.com.nkolay.itam.service.assettype;

import java.util.List;

import tr.com.nkolay.itam.model.dto.AssetTypeResponse;
import tr.com.nkolay.itam.model.dto.AssetTypeUpdateRequest;

public interface AssetTypeService {

    List<AssetTypeResponse> list();

    AssetTypeResponse update(String code, AssetTypeUpdateRequest request);
}
