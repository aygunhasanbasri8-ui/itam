import { apiClient } from './client'
import type { DepartmentSummary, PageResponse } from '../types/common'
import type { DepartmentRequest, DepartmentResponse } from '../types/department'

export async function listActiveDepartments(): Promise<DepartmentSummary[]> {
  const response = await apiClient.get<PageResponse<DepartmentSummary>>('/departments', {
    params: { active: true, size: 200 },
  })
  return response.data.content
}

export interface DepartmentListParams {
  page?: number
  size?: number
  search?: string
  active?: boolean
}

export async function listDepartments(params: DepartmentListParams): Promise<PageResponse<DepartmentResponse>> {
  const response = await apiClient.get<PageResponse<DepartmentResponse>>('/departments', { params })
  return response.data
}

export async function getDepartment(id: number): Promise<DepartmentResponse> {
  const response = await apiClient.get<DepartmentResponse>(`/departments/${id}`)
  return response.data
}

export async function createDepartment(request: DepartmentRequest): Promise<DepartmentResponse> {
  const response = await apiClient.post<DepartmentResponse>('/departments', request)
  return response.data
}

export async function updateDepartment(id: number, request: DepartmentRequest): Promise<DepartmentResponse> {
  const response = await apiClient.put<DepartmentResponse>(`/departments/${id}`, request)
  return response.data
}
