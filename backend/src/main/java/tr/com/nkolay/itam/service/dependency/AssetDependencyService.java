package tr.com.nkolay.itam.service.dependency;

import java.util.List;

import tr.com.nkolay.itam.model.dto.AssetDependencyRequest;
import tr.com.nkolay.itam.model.dto.AssetDependencyResponse;
import tr.com.nkolay.itam.model.dto.DependencyTreeNode;

public interface AssetDependencyService {

    AssetDependencyResponse create(AssetDependencyRequest request);

    void delete(Long id);

    List<AssetDependencyResponse> listByAsset(Long assetId);

    DependencyTreeNode getDependencyTree(Long rootAssetId);
}
