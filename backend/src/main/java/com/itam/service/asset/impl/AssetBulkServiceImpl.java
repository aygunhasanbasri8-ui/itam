package com.itam.service.asset.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itam.model.dto.BulkStatusUpdateRequest;
import com.itam.model.dto.BulkStatusUpdateResponse;
import com.itam.model.entity.BaseAsset;
import com.itam.repository.BaseAssetRepository;
import com.itam.service.asset.AssetBulkService;
import com.itam.validation.ValidationCollector;

@Service
@Transactional
public class AssetBulkServiceImpl implements AssetBulkService {

    private final BaseAssetRepository baseAssetRepository;

    public AssetBulkServiceImpl(BaseAssetRepository baseAssetRepository) {
        this.baseAssetRepository = baseAssetRepository;
    }

    @Override
    public BulkStatusUpdateResponse updateStatus(BulkStatusUpdateRequest request) {
        ValidationCollector errors = ValidationCollector.create();
        List<BaseAsset> assets = new ArrayList<>();

        // Önce hepsi doğrulanıyor, tek tek güncellenmiyor: tek geçersiz kayıt
        // varsa hiçbiri değişmeden kalmalı, "yarısı güncellendi" gibi belirsiz
        // bir ara durum oluşmamalı.
        for (Long id : request.assetIds()) {
            BaseAsset asset = baseAssetRepository.findById(id).orElse(null);
            if (asset == null) {
                errors.add("assetIds", "assetBulunamadi", "Asset bulunamadı", id);
            } else if (asset.isDeleted()) {
                errors.add("assetIds", "silinmisAsset", "Silinmiş asset durumu güncellenemez", id);
            } else {
                assets.add(asset);
            }
        }

        errors.throwIfInvalid();

        assets.forEach(asset -> asset.setStatus(request.status()));
        return new BulkStatusUpdateResponse(assets.size());
    }
}
