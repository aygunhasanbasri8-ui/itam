import type { AssetStatus, PersonSummary } from './common'

export type CloudProvider = 'AWS' | 'AZURE' | 'GCP' | 'OTHER'

export interface CloudAssetResponse {
  id: number
  assetName: string
  status: AssetStatus
  notes: string | null
  responsibilityOwner: PersonSummary | null
  cloudProvider: CloudProvider
  instanceType: string | null
  region: string | null
  accountId: string | null
  customFields: Record<string, string>
  createdAt: string
  updatedAt: string
}

export interface CloudAssetRequest {
  assetName: string
  notes?: string | null
  responsibilityOwnerId?: number | null
  cloudProvider: CloudProvider
  instanceType?: string | null
  region?: string | null
  accountId?: string | null
  customFields?: Record<string, string>
}
