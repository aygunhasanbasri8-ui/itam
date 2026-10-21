import { apiClient } from './client'
import type { AssetTypeCode } from '../types/assetType'
import type { AssetStatus } from '../types/common'

export interface AssetSummary {
  id: number
  assetName: string
  assetTypeCode: AssetTypeCode
  status: AssetStatus
}

export async function searchAssets(q: string, limit = 20): Promise<AssetSummary[]> {
  const response = await apiClient.get<AssetSummary[]>('/assets/search', { params: { q, limit } })
  return response.data
}
