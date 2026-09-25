import { apiClient } from './client'

export interface DashboardSummary {
  totalAssets: number
  countByAssetType: Record<string, number>
  countByStatus: Record<string, number>
  countByEnvironment: Record<string, number>
  licensesExpiringSoon: number
  licensesExpired: number
}

export async function getDashboardSummary(expiringSoonDays = 30): Promise<DashboardSummary> {
  const response = await apiClient.get<DashboardSummary>('/dashboard/summary', {
    params: { expiringSoonDays },
  })
  return response.data
}
