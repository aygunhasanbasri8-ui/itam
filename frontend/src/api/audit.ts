import { apiClient } from './client'
import type { AssetRevision } from '../types/audit'

export async function getAuditTrail(assetId: number): Promise<AssetRevision[]> {
  const response = await apiClient.get<AssetRevision[]>(`/assets/${assetId}/audit-trail`)
  return response.data
}
