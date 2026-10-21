import { useQuery } from '@tanstack/react-query'
import { listCustomFieldDefinitions } from '../api/customFields'
import type { AssetTypeCode } from '../types/assetType'
import type { CustomFieldDefinition } from '../types/customField'

/** Liste ekranında kolon olarak gösterilmek üzere işaretlenmiş aktif özel alanlar. */
export function useListCustomFields(assetTypeCode: AssetTypeCode): CustomFieldDefinition[] {
  const { data } = useQuery({
    queryKey: ['custom-field-definitions', assetTypeCode],
    queryFn: () => listCustomFieldDefinitions(assetTypeCode),
  })
  return data?.filter((definition) => definition.active && definition.showInList) ?? []
}

export function formatCustomFieldValue(definition: CustomFieldDefinition, value: string | undefined): string {
  if (value === undefined || value === '') {
    return '-'
  }
  switch (definition.dataType) {
    case 'BOOLEAN':
      return value === 'true' ? 'Evet' : 'Hayır'
    case 'DATE':
      return new Date(value).toLocaleDateString('tr-TR')
    default:
      return value
  }
}
