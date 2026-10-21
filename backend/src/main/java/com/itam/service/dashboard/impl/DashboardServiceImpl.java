package com.itam.service.dashboard.impl;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itam.enums.AssetStatus;
import com.itam.enums.ServerEnvironment;
import com.itam.mapper.AssetSummaryMapper;
import com.itam.model.dto.DashboardSummaryResponse;
import com.itam.repository.ApplicationAssetRepository;
import com.itam.repository.BaseAssetRepository;
import com.itam.repository.ServerAssetRepository;
import com.itam.service.dashboard.DashboardService;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final BaseAssetRepository baseAssetRepository;
    private final ServerAssetRepository serverAssetRepository;
    private final ApplicationAssetRepository applicationAssetRepository;
    private final AssetSummaryMapper assetSummaryMapper;

    public DashboardServiceImpl(BaseAssetRepository baseAssetRepository,
            ServerAssetRepository serverAssetRepository,
            ApplicationAssetRepository applicationAssetRepository,
            AssetSummaryMapper assetSummaryMapper) {
        this.baseAssetRepository = baseAssetRepository;
        this.serverAssetRepository = serverAssetRepository;
        this.applicationAssetRepository = applicationAssetRepository;
        this.assetSummaryMapper = assetSummaryMapper;
    }

    @Override
    public DashboardSummaryResponse getSummary(int expiringSoonDays) {
        LocalDate today = LocalDate.now();

        Map<String, Long> byStatus = toMap(baseAssetRepository.countByStatusGrouped(),
                row -> ((AssetStatus) row[0]).name());
        Map<String, Long> byType = toMap(baseAssetRepository.countByAssetTypeGrouped(),
                row -> (String) row[0]);
        Map<String, Long> byEnvironment = toMap(serverAssetRepository.countByEnvironmentGrouped(),
                row -> ((ServerEnvironment) row[0]).name());

        long expired = applicationAssetRepository.countByDeletedFalseAndExpiryDateBefore(today);
        long expiringSoon = applicationAssetRepository
                .countByDeletedFalseAndExpiryDateBetween(today, today.plusDays(expiringSoonDays));

        return new DashboardSummaryResponse(
                baseAssetRepository.countByDeletedFalse(),
                byType,
                byStatus,
                byEnvironment,
                expiringSoon,
                expired,
                baseAssetRepository.findTop5ByDeletedFalseOrderByCreatedAtDesc().stream()
                        .map(assetSummaryMapper::toSummary)
                        .toList());
    }

    private Map<String, Long> toMap(List<Object[]> rows, Function<Object[], String> keyExtractor) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put(keyExtractor.apply(row), (Long) row[1]);
        }
        return map;
    }
}
