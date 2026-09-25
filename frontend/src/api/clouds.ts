import { apiClient } from './client'
import type { PageResponse } from '../types/common'
import type { CloudAssetRequest, CloudAssetResponse } from '../types/cloud'

export interface CloudListParams {
  page?: number
  size?: number
  provider?: string
  region?: string
  status?: string
}

export async function listClouds(params: CloudListParams): Promise<PageResponse<CloudAssetResponse>> {
  const response = await apiClient.get<PageResponse<CloudAssetResponse>>('/cloud-assets', { params })
  return response.data
}

export async function getCloud(id: number): Promise<CloudAssetResponse> {
  const response = await apiClient.get<CloudAssetResponse>(`/cloud-assets/${id}`)
  return response.data
}

export async function createCloud(request: CloudAssetRequest): Promise<CloudAssetResponse> {
  const response = await apiClient.post<CloudAssetResponse>('/cloud-assets', request)
  return response.data
}

export async function updateCloud(id: number, request: CloudAssetRequest): Promise<CloudAssetResponse> {
  const response = await apiClient.put<CloudAssetResponse>(`/cloud-assets/${id}`, request)
  return response.data
}
