export type DependencyType = 'RUNS_ON' | 'USES' | 'DEPENDS_ON'

export interface AssetDependencyResponse {
  id: number
  parentAssetId: number
  parentAssetName: string
  childAssetId: number
  childAssetName: string
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
  // Kök düğümde her zaman null
  dependencyType: DependencyType | null
  children: DependencyTreeNode[]
}
