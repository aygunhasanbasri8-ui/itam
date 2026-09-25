import type { AssetStatus, DepartmentSummary, PersonSummary } from './common'

export interface WorkstationAssetResponse {
  id: number
  assetName: string
  status: AssetStatus
  notes: string | null
  responsibilityOwner: PersonSummary | null
  serialNumber: string
  model: string | null
  cpu: string | null
  ramGb: number | null
  diskGb: number | null
  assignedPerson: PersonSummary | null
  department: DepartmentSummary | null
  customFields: Record<string, string>
  createdAt: string
  updatedAt: string
}

export interface WorkstationAssetRequest {
  assetName: string
  notes?: string | null
  responsibilityOwnerId?: number | null
  serialNumber: string
  model?: string | null
  cpu?: string | null
  ramGb?: number | null
  diskGb?: number | null
  assignedPersonId?: number | null
  departmentId?: number | null
  customFields?: Record<string, string>
}
