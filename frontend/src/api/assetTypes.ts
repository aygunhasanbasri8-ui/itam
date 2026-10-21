import { apiClient } from './client'

export interface AssetTypeDefinition {
  code: string
  displayName: string
  kind: string
  icon: string
  description: string | null
  system: boolean
  active: boolean
  sortOrder: number
}

export interface AssetTypeUpdateRequest {
  displayName: string
  icon: string
  description: string | null
  sortOrder: number
}

export async function listAssetTypes(): Promise<AssetTypeDefinition[]> {
  const response = await apiClient.get<AssetTypeDefinition[]>('/asset-types')
  return response.data
}

export async function updateAssetType(code: string, request: AssetTypeUpdateRequest): Promise<AssetTypeDefinition> {
  const response = await apiClient.put<AssetTypeDefinition>(`/asset-types/${code}`, request)
  return response.data
}
