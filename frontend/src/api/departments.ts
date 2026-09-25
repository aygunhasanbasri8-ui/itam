import { apiClient } from './client'
import type { DepartmentSummary, PageResponse } from '../types/common'

export async function listActiveDepartments(): Promise<DepartmentSummary[]> {
  const response = await apiClient.get<PageResponse<DepartmentSummary>>('/departments', {
    params: { active: true, size: 200 },
  })
  return response.data.content
}
