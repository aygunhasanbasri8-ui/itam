package tr.com.nkolay.itam.service.asset.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.exception.ResourceNotFoundException;
import tr.com.nkolay.itam.model.entity.BaseAsset;
import tr.com.nkolay.itam.repository.AssetDependencyRepository;
import tr.com.nkolay.itam.repository.BaseAssetRepository;
import tr.com.nkolay.itam.service.asset.AssetDeletionService;
import tr.com.nkolay.itam.validation.ValidationCollector;

@Service
@Transactional
public class AssetDeletionServiceImpl implements AssetDeletionService {

    private final BaseAssetRepository baseAssetRepository;
    private final AssetDependencyRepository dependencyRepository;

    public AssetDeletionServiceImpl(BaseAssetRepository baseAssetRepository,
            AssetDependencyRepository dependencyRepository) {
        this.baseAssetRepository = baseAssetRepository;
        this.dependencyRepository = dependencyRepository;
    }

    @Override
    public void softDelete(Long assetId) {
        BaseAsset asset = baseAssetRepository.findById(assetId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId));

        // Kenarları sessizce silmek yerine reddediyoruz: bir iş servisinin hangi
        // sunucuya bağlı olduğu bilgisi bir silme işleminin yan etkisiyle kaybolmasın.
        ValidationCollector errors = ValidationCollector.create();
        errors.check(!dependencyRepository.existsByParentAssetIdOrChildAssetId(assetId, assetId),
                "id", "bagimlilikVar",
                "Bu varlığın bağımlılıkları var; silmeden önce bağımlılıkları kaldırın", assetId);
        errors.throwIfInvalid();

        asset.setDeleted(true);
    }
}
