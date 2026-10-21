package com.itam.model.dto;

import java.util.List;
import java.util.Map;

/**
 * @param countByAssetType   asset_type_def.code -> silinmemiş kayıt sayısı
 * @param countByStatus      AssetStatus adı -> silinmemiş kayıt sayısı
 * @param countByEnvironment ServerEnvironment adı -> silinmemiş sunucu sayısı (yalnızca sunucular)
 * @param licensesExpiringSoon istek parametresindeki gün sayısı içinde süresi dolacak uygulama sayısı
 * @param licensesExpired      süresi bugünden önce dolmuş uygulama sayısı
 * @param recentAssets         en son eklenen silinmemiş asset'ler, yeniden eskiye
 */
public record DashboardSummaryResponse(
        long totalAssets,
        Map<String, Long> countByAssetType,
        Map<String, Long> countByStatus,
        Map<String, Long> countByEnvironment,
        long licensesExpiringSoon,
        long licensesExpired,
        List<AssetSummaryResponse> recentAssets) {
}
