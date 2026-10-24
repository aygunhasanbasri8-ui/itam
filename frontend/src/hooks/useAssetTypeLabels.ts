import { useQuery } from '@tanstack/react-query'
import { listAssetTypes, type AssetTypeDefinition } from '../api/assetTypes'
import { ASSET_TYPE_LABELS } from '../types/assetType'

/**
 * Tip adları yönetici ekranından değiştirilebiliyor (Asset Tipleri); sabit tablo yalnızca
 * istek dönene kadar ve istek başarısız olursa kullanılıyor, ekran boş kalmasın.
 */
export function mergeAssetTypeLabels(types: AssetTypeDefinition[] | undefined): Record<string, string> {
  const labels: Record<string, string> = { ...ASSET_TYPE_LABELS }
  for (const type of types ?? []) {
    labels[type.code] = type.displayName
  }
  return labels
}

/** Filtre listeleri için: etkin tipler, yöneticinin verdiği sırayla. */
export function activeAssetTypes(types: AssetTypeDefinition[] | undefined): { code: string; label: string }[] {
  if (!types?.length) {
    return Object.entries(ASSET_TYPE_LABELS).map(([code, label]) => ({ code, label }))
  }
  return [...types]
    .filter((type) => type.active)
    .sort((a, b) => a.sortOrder - b.sortOrder)
    .map((type) => ({ code: type.code, label: type.displayName }))
}

export function useAssetTypes() {
  const { data } = useQuery({ queryKey: ['asset-types'], queryFn: listAssetTypes, staleTime: 5 * 60 * 1000 })
  return { labels: mergeAssetTypeLabels(data), active: activeAssetTypes(data) }
}
