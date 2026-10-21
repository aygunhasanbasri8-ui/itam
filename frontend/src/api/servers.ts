import { apiClient } from './client'
import type { PageResponse } from '../types/common'
import type { ServerAssetRequest, ServerAssetResponse } from '../types/server'

export interface ServerListParams {
  page?: number
  size?: number
  responsibilityOwnerId?: number
  hostname?: string
  environment?: string
  status?: string
}

export async function listServers(params: ServerListParams): Promise<PageResponse<ServerAssetResponse>> {
  const response = await apiClient.get<PageResponse<ServerAssetResponse>>('/servers', { params })
  return response.data
}

export async function getServer(id: number): Promise<ServerAssetResponse> {
  const response = await apiClient.get<ServerAssetResponse>(`/servers/${id}`)
  return response.data
}

export async function createServer(request: ServerAssetRequest): Promise<ServerAssetResponse> {
  const response = await apiClient.post<ServerAssetResponse>('/servers', request)
  return response.data
}

export async function updateServer(id: number, request: ServerAssetRequest): Promise<ServerAssetResponse> {
  const response = await apiClient.put<ServerAssetResponse>(`/servers/${id}`, request)
  return response.data
}
