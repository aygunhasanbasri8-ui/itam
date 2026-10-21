import { apiClient } from './client'
import type { AssetDependencyRequest, AssetDependencyResponse, DependencyTreeNode } from '../types/dependency'

export async function listAssetDependencies(assetId: number): Promise<AssetDependencyResponse[]> {
  const response = await apiClient.get<AssetDependencyResponse[]>(`/assets/${assetId}/dependencies`)
  return response.data
}

export async function getDependencyTree(assetId: number): Promise<DependencyTreeNode> {
  const response = await apiClient.get<DependencyTreeNode>(`/assets/${assetId}/dependency-tree`)
  return response.data
}

export async function createDependency(request: AssetDependencyRequest): Promise<AssetDependencyResponse> {
  const response = await apiClient.post<AssetDependencyResponse>('/asset-dependencies', request)
  return response.data
}

export async function deleteDependency(id: number): Promise<void> {
  await apiClient.delete(`/asset-dependencies/${id}`)
}
