export interface PersonSummary {
  id: number
  fullName: string
}

export interface DepartmentSummary {
  id: number
  name: string
}

export type AssetStatus = 'ACTIVE' | 'RETIRED' | 'MAINTENANCE'

export const ASSET_STATUS_LABELS: Record<AssetStatus, string> = {
  ACTIVE: 'Aktif',
  RETIRED: 'Hizmet Dışı',
  MAINTENANCE: 'Bakımda',
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}
