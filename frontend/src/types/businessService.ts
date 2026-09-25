import type { AssetStatus, PersonSummary } from './common'

export type ServiceCriticality = 'HIGH' | 'MID' | 'LOW'

export interface BusinessServiceAssetResponse {
  id: number
  assetName: string
  status: AssetStatus
  notes: string | null
  responsibilityOwner: PersonSummary | null
  criticality: ServiceCriticality
  sla: string | null
  serviceOwner: PersonSummary | null
  customFields: Record<string, string>
  createdAt: string
  updatedAt: string
}

export interface BusinessServiceAssetRequest {
  assetName: string
  notes?: string | null
  responsibilityOwnerId?: number | null
  criticality?: ServiceCriticality | null
  sla?: string | null
  serviceOwnerId?: number | null
  customFields?: Record<string, string>
}
