import { apiClient } from './client'
import type { AssetTypeCode } from '../types/assetType'
import type {
  CustomFieldDefinition,
  CustomFieldDefinitionRequest,
  CustomFieldDefinitionUpdateRequest,
} from '../types/customField'

export async function listCustomFieldDefinitions(assetTypeCode: AssetTypeCode): Promise<CustomFieldDefinition[]> {
  const response = await apiClient.get<CustomFieldDefinition[]>('/custom-field-definitions', {
    params: { assetTypeCode },
  })
  return response.data
}

export async function createCustomFieldDefinition(
  request: CustomFieldDefinitionRequest,
): Promise<CustomFieldDefinition> {
  const response = await apiClient.post<CustomFieldDefinition>('/custom-field-definitions', request)
  return response.data
}

export async function updateCustomFieldDefinition(
  id: number,
  request: CustomFieldDefinitionUpdateRequest,
): Promise<CustomFieldDefinition> {
  const response = await apiClient.put<CustomFieldDefinition>(`/custom-field-definitions/${id}`, request)
  return response.data
}
