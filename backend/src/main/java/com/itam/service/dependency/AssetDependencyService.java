package com.itam.service.dependency;

import java.util.List;

import com.itam.model.dto.AssetDependencyRequest;
import com.itam.model.dto.AssetDependencyResponse;
import com.itam.model.dto.DependencyTreeNode;

public interface AssetDependencyService {

    AssetDependencyResponse create(AssetDependencyRequest request);

    void delete(Long id);

    List<AssetDependencyResponse> listByAsset(Long assetId);

    DependencyTreeNode getDependencyTree(Long rootAssetId);
}
