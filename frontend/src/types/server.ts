import type { AssetStatus, PersonSummary } from './common'

export type ServerEnvironment = 'DEV' | 'TEST' | 'UAT' | 'PROD'

export interface ServerAssetResponse {
  id: number
  assetName: string
  status: AssetStatus
  notes: string | null
  responsibilityOwner: PersonSummary | null
  domain: string | null
  hostname: string
  ipAddress: string | null
  environment: ServerEnvironment
  serverClass: string | null
  groupName: string | null
  groupInfo: string | null
  groupDesc: string | null
  infrastructureType: string | null
  os: string | null
  virtual: boolean
  serviceOwner: PersonSummary | null
  appResponsible: PersonSummary | null
  osResponsible: PersonSummary | null
  customFields: Record<string, string>
  createdAt: string
  updatedAt: string
}

export interface ServerAssetRequest {
  assetName: string
  notes?: string | null
  responsibilityOwnerId?: number | null
  domain?: string | null
  hostname: string
  ipAddress?: string | null
  environment: ServerEnvironment
  serverClass?: string | null
  groupName?: string | null
  groupInfo?: string | null
  groupDesc?: string | null
  infrastructureType?: string | null
  os?: string | null
  virtual: boolean
  serviceOwnerId?: number | null
  appResponsibleId?: number | null
  osResponsibleId?: number | null
  customFields?: Record<string, string>
}
