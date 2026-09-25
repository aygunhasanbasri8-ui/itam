export interface PersonSummary {
  id: number
  fullName: string
}

export interface DepartmentSummary {
  id: number
  name: string
}

export type AssetStatus = 'ACTIVE' | 'RETIRED' | 'MAINTENANCE'

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}
