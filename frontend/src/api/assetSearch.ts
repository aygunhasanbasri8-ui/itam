import { apiClient } from './client'
import type { AssetTypeCode } from '../types/assetType'
import type { AssetStatus, PageResponse, PersonSummary } from '../types/common'

export interface AssetSummary {
  id: number
  assetName: string
  assetTypeCode: AssetTypeCode
  status: AssetStatus
  responsibilityOwner: PersonSummary | null
  createdAt: string
}

export async function searchAssets(q: string, limit = 20): Promise<AssetSummary[]> {
  const response = await apiClient.get<AssetSummary[]>('/assets/search', { params: { q, limit } })
  return response.data
}

export interface AssetListParams {
  q?: string
  assetType?: string
  status?: string
  responsibilityOwnerId?: number
  page?: number
  size?: number
}

export async function listAllAssets(params: AssetListParams): Promise<PageResponse<AssetSummary>> {
  const response = await apiClient.get<PageResponse<AssetSummary>>('/assets', { params })
  return response.data
}
