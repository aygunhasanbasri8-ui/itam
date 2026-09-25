import type { AssetStatus, PersonSummary } from './common'

export interface ApplicationAssetResponse {
  id: number
  assetName: string
  status: AssetStatus
  notes: string | null
  responsibilityOwner: PersonSummary | null
  version: string | null
  licenseType: string | null
  licenseCount: number | null
  vendor: string | null
  expiryDate: string | null
  installPath: string | null
  customFields: Record<string, string>
  createdAt: string
  updatedAt: string
}

export interface ApplicationAssetRequest {
  assetName: string
  notes?: string | null
  responsibilityOwnerId?: number | null
  version?: string | null
  licenseType?: string | null
  licenseCount?: number | null
  vendor?: string | null
  expiryDate?: string | null
  installPath?: string | null
  customFields?: Record<string, string>
}
