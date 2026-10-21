import { useQuery } from '@tanstack/react-query'
import { listCustomFieldDefinitions } from '../api/customFields'
import { Field } from './Field'
import type { AssetTypeCode } from '../types/assetType'
import type { CustomFieldDefinition } from '../types/customField'

interface Props {
  assetTypeCode: AssetTypeCode
  values: Record<string, string> | undefined
  onChange: (values: Record<string, string>) => void
}

export function CustomFieldInputs({ assetTypeCode, values = {}, onChange }: Props) {
  const { data: definitions } = useQuery({
    queryKey: ['custom-field-definitions', assetTypeCode],
    queryFn: () => listCustomFieldDefinitions(assetTypeCode),
  })

  // Pasif alanı sadece asset'te değeri varsa salt okunur gösteriyoruz; değer
  // map'te kaldığı için kaydederken kaybolmuyor.
  const visible = definitions?.filter((definition) => definition.active || values[definition.fieldKey]) ?? []
  if (visible.length === 0) {
    return null
  }

  function setValue(key: string, value: string) {
    const next = { ...values }
    // Boş değeri hiç göndermiyoruz; aksi halde NUMBER/DATE alanında "" biçim hatasına düşer
    if (value === '') {
      delete next[key]
    } else {
      next[key] = value
    }
    onChange(next)
  }

  return (
    <fieldset className="space-y-3 border-t border-slate-100 pt-4">
      <legend className="text-sm font-semibold text-slate-700">Özel Alanlar</legend>
      <div className="grid grid-cols-2 gap-4">
        {visible.map((definition) => (
          <Field key={definition.id} label={definition.active ? definition.label : `${definition.label} (pasif)`}>
            <CustomFieldInput
              definition={definition}
              value={values[definition.fieldKey] ?? ''}
              onChange={(value) => setValue(definition.fieldKey, value)}
            />
          </Field>
        ))}
      </div>
    </fieldset>
  )
}

function CustomFieldInput({
  definition,
  value,
  onChange,
}: {
  definition: CustomFieldDefinition
  value: string
  onChange: (value: string) => void
}) {
  const disabled = !definition.active

  switch (definition.dataType) {
    case 'BOOLEAN':
      return (
        <select className="input" value={value} disabled={disabled} onChange={(e) => onChange(e.target.value)}>
          <option value="">—</option>
          <option value="true">Evet</option>
          <option value="false">Hayır</option>
        </select>
      )
    case 'NUMBER':
      return (
        <input
          type="number"
          step="any"
          className="input"
          value={value}
          disabled={disabled}
          onChange={(e) => onChange(e.target.value)}
        />
      )
    case 'DATE':
      return (
        <input
          type="date"
          className="input"
          value={value}
          disabled={disabled}
          onChange={(e) => onChange(e.target.value)}
        />
      )
    default:
      return (
        <input className="input" value={value} disabled={disabled} onChange={(e) => onChange(e.target.value)} />
      )
  }
}
