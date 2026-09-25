import { apiClient } from './client'
import type { PageResponse } from '../types/common'
import type { ApplicationAssetRequest, ApplicationAssetResponse } from '../types/application'

export interface ApplicationListParams {
  page?: number
  size?: number
  vendor?: string
  status?: string
  expiryBefore?: string
}

export async function listApplications(
  params: ApplicationListParams,
): Promise<PageResponse<ApplicationAssetResponse>> {
  const response = await apiClient.get<PageResponse<ApplicationAssetResponse>>('/applications', { params })
  return response.data
}

export async function getApplication(id: number): Promise<ApplicationAssetResponse> {
  const response = await apiClient.get<ApplicationAssetResponse>(`/applications/${id}`)
  return response.data
}

export async function createApplication(request: ApplicationAssetRequest): Promise<ApplicationAssetResponse> {
  const response = await apiClient.post<ApplicationAssetResponse>('/applications', request)
  return response.data
}

export async function updateApplication(
  id: number,
  request: ApplicationAssetRequest,
): Promise<ApplicationAssetResponse> {
  const response = await apiClient.put<ApplicationAssetResponse>(`/applications/${id}`, request)
  return response.data
}
