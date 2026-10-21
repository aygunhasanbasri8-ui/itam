import type { DepartmentSummary } from './common'

export interface PersonResponse {
  id: number
  fullName: string
  code: string | null
  email: string | null
  title: string | null
  phone: string | null
  department: DepartmentSummary | null
  // FIRMA kaydı; backend düzenlemeye izin vermiyor
  system: boolean
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface PersonRequest {
  fullName: string
  code?: string | null
  email?: string | null
  title?: string | null
  phone?: string | null
  departmentId?: number | null
  active: boolean
}
