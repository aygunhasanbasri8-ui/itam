export type AssetTypeCode = 'SERVER' | 'WORKSTATION' | 'CLOUD' | 'APPLICATION' | 'BUSINESS_SERVICE'

export const ASSET_TYPE_LABELS: Record<AssetTypeCode, string> = {
  SERVER: 'Sunucu',
  WORKSTATION: 'İş İstasyonu',
  CLOUD: 'Cloud Varlığı',
  APPLICATION: 'Uygulama & Yazılım',
  BUSINESS_SERVICE: 'İş Servisi',
}

// Tip koduna göre detay ekranının yolu; tipten bağımsız listelerden (arama, son eklenenler) gitmek için
export const ASSET_TYPE_ROUTES: Record<AssetTypeCode, string> = {
  SERVER: '/servers',
  WORKSTATION: '/workstations',
  CLOUD: '/clouds',
  APPLICATION: '/applications',
  BUSINESS_SERVICE: '/business-services',
}
