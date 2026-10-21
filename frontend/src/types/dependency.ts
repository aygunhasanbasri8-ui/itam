import type { AssetTypeCode } from './assetType'

export type DependencyType = 'RUNS_ON' | 'USES' | 'DEPENDS_ON'

export interface AssetDependencyResponse {
  id: number
  parentAssetId: number
  parentAssetName: string
  parentAssetTypeCode: AssetTypeCode
  childAssetId: number
  childAssetName: string
  childAssetTypeCode: AssetTypeCode
  dependencyType: DependencyType
  notes: string | null
  createdAt: string
  createdBy: string | null
}

export interface AssetDependencyRequest {
  parentAssetId: number
  childAssetId: number
  dependencyType: DependencyType
  notes?: string | null
}

export interface DependencyTreeNode {
  assetId: number
  assetName: string
  assetTypeCode: AssetTypeCode
  // Kök düğümde her zaman null
  dependencyType: DependencyType | null
  children: DependencyTreeNode[]
}
