package tr.com.nkolay.itam.service.asset.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.mapper.AssetSummaryMapper;
import tr.com.nkolay.itam.model.dto.AssetSummaryResponse;
import tr.com.nkolay.itam.repository.BaseAssetRepository;
import tr.com.nkolay.itam.service.asset.AssetSearchService;

@Service
@Transactional(readOnly = true)
public class AssetSearchServiceImpl implements AssetSearchService {

    // Seçim kutusu için arama; daha fazlası gerekiyorsa tip bazlı liste ekranları var
    private static final int MAX_LIMIT = 50;

    private final BaseAssetRepository baseAssetRepository;
    private final AssetSummaryMapper mapper;

    public AssetSearchServiceImpl(BaseAssetRepository baseAssetRepository, AssetSummaryMapper mapper) {
        this.baseAssetRepository = baseAssetRepository;
        this.mapper = mapper;
    }

    @Override
    public List<AssetSummaryResponse> search(String q, int limit) {
        int size = Math.clamp(limit, 1, MAX_LIMIT);
        String term = q == null ? "" : q.trim();
        return baseAssetRepository
                .findByDeletedFalseAndAssetNameContainingIgnoreCaseOrderByAssetName(term, PageRequest.of(0, size))
                .stream()
                .map(mapper::toSummary)
                .toList();
    }
}
