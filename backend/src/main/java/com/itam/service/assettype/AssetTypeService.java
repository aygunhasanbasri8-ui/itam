package com.itam.service.assettype;

import java.util.List;

import com.itam.model.dto.AssetTypeResponse;
import com.itam.model.dto.AssetTypeUpdateRequest;

public interface AssetTypeService {

    List<AssetTypeResponse> list();

    AssetTypeResponse update(String code, AssetTypeUpdateRequest request);
}
