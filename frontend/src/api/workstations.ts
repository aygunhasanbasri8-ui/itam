import { apiClient } from './client'
import type { PageResponse } from '../types/common'
import type { WorkstationAssetRequest, WorkstationAssetResponse } from '../types/workstation'

export interface WorkstationListParams {
  page?: number
  size?: number
  responsibilityOwnerId?: number
  serialNumber?: string
  status?: string
  departmentId?: number
  assignedPersonId?: number
}

export async function listWorkstations(
  params: WorkstationListParams,
): Promise<PageResponse<WorkstationAssetResponse>> {
  const response = await apiClient.get<PageResponse<WorkstationAssetResponse>>('/workstations', { params })
  return response.data
}

export async function getWorkstation(id: number): Promise<WorkstationAssetResponse> {
  const response = await apiClient.get<WorkstationAssetResponse>(`/workstations/${id}`)
  return response.data
}

export async function createWorkstation(request: WorkstationAssetRequest): Promise<WorkstationAssetResponse> {
  const response = await apiClient.post<WorkstationAssetResponse>('/workstations', request)
  return response.data
}

export async function updateWorkstation(
  id: number,
  request: WorkstationAssetRequest,
): Promise<WorkstationAssetResponse> {
  const response = await apiClient.put<WorkstationAssetResponse>(`/workstations/${id}`, request)
  return response.data
}
