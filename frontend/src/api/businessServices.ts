import { apiClient } from './client'
import type { PageResponse } from '../types/common'
import type { BusinessServiceAssetRequest, BusinessServiceAssetResponse } from '../types/businessService'

export interface BusinessServiceListParams {
  page?: number
  size?: number
  responsibilityOwnerId?: number
  criticality?: string
  status?: string
}

export async function listBusinessServices(
  params: BusinessServiceListParams,
): Promise<PageResponse<BusinessServiceAssetResponse>> {
  const response = await apiClient.get<PageResponse<BusinessServiceAssetResponse>>('/business-services', { params })
  return response.data
}

export async function getBusinessService(id: number): Promise<BusinessServiceAssetResponse> {
  const response = await apiClient.get<BusinessServiceAssetResponse>(`/business-services/${id}`)
  return response.data
}

export async function createBusinessService(
  request: BusinessServiceAssetRequest,
): Promise<BusinessServiceAssetResponse> {
  const response = await apiClient.post<BusinessServiceAssetResponse>('/business-services', request)
  return response.data
}

export async function updateBusinessService(
  id: number,
  request: BusinessServiceAssetRequest,
): Promise<BusinessServiceAssetResponse> {
  const response = await apiClient.put<BusinessServiceAssetResponse>(`/business-services/${id}`, request)
  return response.data
}
