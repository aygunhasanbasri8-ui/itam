import type { AssetTypeCode } from './assetType'

export type CustomFieldType = 'TEXT' | 'NUMBER' | 'DATE' | 'BOOLEAN'

export interface CustomFieldDefinition {
  id: number
  assetTypeCode: AssetTypeCode
  fieldKey: string
  label: string
  dataType: CustomFieldType
  showInList: boolean
  active: boolean
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export interface CustomFieldDefinitionRequest {
  assetTypeCode: AssetTypeCode
  fieldKey: string
  label: string
  dataType: CustomFieldType
  showInList: boolean
  sortOrder: number
}

// assetTypeCode/fieldKey/dataType oluşturulduktan sonra değiştirilemiyor
export interface CustomFieldDefinitionUpdateRequest {
  label: string
  showInList: boolean
  active: boolean
  sortOrder: number
}
