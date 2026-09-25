package tr.com.nkolay.itam.model.dto;

import java.util.Map;

/**
 * @param countByAssetType   asset_type_def.code -> silinmemiş kayıt sayısı
 * @param countByStatus      AssetStatus adı -> silinmemiş kayıt sayısı
 * @param countByEnvironment ServerEnvironment adı -> silinmemiş sunucu sayısı (yalnızca sunucular)
 * @param licensesExpiringSoon istek parametresindeki gün sayısı içinde süresi dolacak uygulama sayısı
 * @param licensesExpired      süresi bugünden önce dolmuş uygulama sayısı
 */
public record DashboardSummaryResponse(
        long totalAssets,
        Map<String, Long> countByAssetType,
        Map<String, Long> countByStatus,
        Map<String, Long> countByEnvironment,
        long licensesExpiringSoon,
        long licensesExpired) {
}
