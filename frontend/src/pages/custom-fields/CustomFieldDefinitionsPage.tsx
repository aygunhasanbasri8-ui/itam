import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createCustomFieldDefinition,
  listCustomFieldDefinitions,
  updateCustomFieldDefinition,
} from '../../api/customFields'
import { extractErrorMessage } from '../../api/client'
import { useIsAdmin } from '../../auth/useIsAdmin'
import { Field } from '../../components/Field'
import { useAssetTypes } from '../../hooks/useAssetTypeLabels'
import { ASSET_TYPE_LABELS, type AssetTypeCode } from '../../types/assetType'
import type {
  CustomFieldDefinition,
  CustomFieldDefinitionRequest,
  CustomFieldType,
} from '../../types/customField'

const DATA_TYPE_LABELS: Record<CustomFieldType, string> = {
  TEXT: 'Metin',
  NUMBER: 'Sayı',
  DATE: 'Tarih',
  BOOLEAN: 'Evet/Hayır',
}

export function CustomFieldDefinitionsPage() {
  const { labels: assetTypeLabels } = useAssetTypes()
  const [assetType, setAssetType] = useState<AssetTypeCode>('SERVER')
  const canEdit = useIsAdmin()

  const { data: definitions, isLoading, isError } = useQuery({
    queryKey: ['custom-field-definitions', assetType],
    queryFn: () => listCustomFieldDefinitions(assetType),
  })

  return (
    <div className="max-w-4xl space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Özel Alanlar</h1>
        <select
          className="rounded border border-slate-300 px-3 py-2 text-sm"
          value={assetType}
          onChange={(e) => setAssetType(e.target.value as AssetTypeCode)}
        >
          {Object.keys(ASSET_TYPE_LABELS).map((code) => (
            <option key={code} value={code}>
              {assetTypeLabels[code]}
            </option>
          ))}
        </select>
      </div>

      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white">
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-500">
            <tr>
              <th className="px-4 py-2 font-medium">Anahtar</th>
              <th className="px-4 py-2 font-medium">Etiket</th>
              <th className="px-4 py-2 font-medium">Tip</th>
              <th className="px-4 py-2 font-medium">Sıra</th>
              <th className="px-4 py-2 font-medium">Listede</th>
              <th className="px-4 py-2 font-medium">Aktif</th>
              {canEdit && <th className="px-4 py-2" />}
            </tr>
          </thead>
          <tbody>
            {isLoading && (
              <tr>
                <td colSpan={7} className="px-4 py-6 text-center text-slate-500">
                  Yükleniyor...
                </td>
              </tr>
            )}
            {isError && (
              <tr>
                <td colSpan={7} className="px-4 py-6 text-center text-red-600">
                  Özel alanlar alınamadı.
                </td>
              </tr>
            )}
            {definitions?.length === 0 && (
              <tr>
                <td colSpan={7} className="px-4 py-6 text-center text-slate-400">
                  Bu asset tipi için tanımlı özel alan yok.
                </td>
              </tr>
            )}
            {definitions?.map((definition) => (
              <DefinitionRow key={definition.id} definition={definition} canEdit={canEdit} />
            ))}
          </tbody>
        </table>
      </div>

      {/* key: tip değişince formdaki yarım kalmış giriş eski tipe ait kalmasın */}
      {canEdit && <NewDefinitionForm key={assetType} assetType={assetType} />}
    </div>
  )
}

function DefinitionRow({ definition, canEdit }: { definition: CustomFieldDefinition; canEdit: boolean }) {
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState(false)
  const [draft, setDraft] = useState({
    label: definition.label,
    showInList: definition.showInList,
    active: definition.active,
    sortOrder: definition.sortOrder,
  })
  const [error, setError] = useState<string | null>(null)

  const mutation = useMutation({
    mutationFn: () => updateCustomFieldDefinition(definition.id, draft),
    onSuccess: () => {
      setEditing(false)
      setError(null)
      queryClient.invalidateQueries({ queryKey: ['custom-field-definitions', definition.assetTypeCode] })
    },
    onError: (err) => setError(extractErrorMessage(err)),
  })

  if (!editing) {
    return (
      <tr className="border-t border-slate-100">
        <td className="px-4 py-2 font-mono text-xs text-slate-600">{definition.fieldKey}</td>
        <td className="px-4 py-2 text-slate-700">{definition.label}</td>
        <td className="px-4 py-2 text-slate-600">{DATA_TYPE_LABELS[definition.dataType]}</td>
        <td className="px-4 py-2 text-slate-600">{definition.sortOrder}</td>
        <td className="px-4 py-2 text-slate-600">{definition.showInList ? 'Evet' : 'Hayır'}</td>
        <td className="px-4 py-2 text-slate-600">{definition.active ? 'Evet' : 'Hayır'}</td>
        {canEdit && (
          <td className="px-4 py-2 text-right">
            <button type="button" onClick={() => setEditing(true)} className="text-slate-600 hover:underline">
              Düzenle
            </button>
          </td>
        )}
      </tr>
    )
  }

  return (
    <tr className="border-t border-slate-100 bg-slate-50">
      <td className="px-4 py-2 font-mono text-xs text-slate-600">{definition.fieldKey}</td>
      <td className="px-4 py-2">
        <input
          className="input"
          value={draft.label}
          onChange={(e) => setDraft({ ...draft, label: e.target.value })}
        />
        {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
      </td>
      <td className="px-4 py-2 text-slate-600">{DATA_TYPE_LABELS[definition.dataType]}</td>
      <td className="px-4 py-2">
        <input
          type="number"
          className="input w-20"
          value={draft.sortOrder}
          onChange={(e) => setDraft({ ...draft, sortOrder: Number(e.target.value) })}
        />
      </td>
      <td className="px-4 py-2">
        <input
          type="checkbox"
          checked={draft.showInList}
          onChange={(e) => setDraft({ ...draft, showInList: e.target.checked })}
        />
      </td>
      <td className="px-4 py-2">
        <input
          type="checkbox"
          checked={draft.active}
          onChange={(e) => setDraft({ ...draft, active: e.target.checked })}
        />
      </td>
      <td className="space-x-2 px-4 py-2 text-right whitespace-nowrap">
        <button
          type="button"
          disabled={mutation.isPending}
          onClick={() => mutation.mutate()}
          className="font-medium text-slate-900 hover:underline disabled:opacity-50"
        >
          Kaydet
        </button>
        <button type="button" onClick={() => setEditing(false)} className="text-slate-500 hover:underline">
          Vazgeç
        </button>
      </td>
    </tr>
  )
}

function NewDefinitionForm({ assetType }: { assetType: AssetTypeCode }) {
  const { labels: assetTypeLabels } = useAssetTypes()
  const queryClient = useQueryClient()
  const emptyForm: CustomFieldDefinitionRequest = {
    assetTypeCode: assetType,
    fieldKey: '',
    label: '',
    dataType: 'TEXT',
    showInList: false,
    sortOrder: 0,
  }
  const [form, setForm] = useState<CustomFieldDefinitionRequest>(emptyForm)
  const [error, setError] = useState<string | null>(null)

  const mutation = useMutation({
    mutationFn: createCustomFieldDefinition,
    onSuccess: () => {
      setForm(emptyForm)
      setError(null)
      queryClient.invalidateQueries({ queryKey: ['custom-field-definitions', assetType] })
    },
    onError: (err) => setError(extractErrorMessage(err)),
  })

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    mutation.mutate(form)
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4 rounded-lg border border-slate-200 bg-white p-6">
      <h2 className="text-lg font-semibold text-slate-800">Yeni özel alan — {assetTypeLabels[assetType]}</h2>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Anahtar" required>
          <input
            className="input font-mono"
            placeholder="ornekAlan"
            pattern="[a-zA-Z][a-zA-Z0-9_]*"
            title="Harfle başlamalı; yalnızca harf, rakam ve alt çizgi"
            maxLength={60}
            value={form.fieldKey}
            onChange={(e) => setForm({ ...form, fieldKey: e.target.value })}
            required
          />
        </Field>
        <Field label="Etiket" required>
          <input
            className="input"
            maxLength={120}
            value={form.label}
            onChange={(e) => setForm({ ...form, label: e.target.value })}
            required
          />
        </Field>
        <Field label="Veri tipi" required>
          <select
            className="input"
            value={form.dataType}
            onChange={(e) => setForm({ ...form, dataType: e.target.value as CustomFieldType })}
          >
            {Object.entries(DATA_TYPE_LABELS).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </Field>
        <Field label="Sıra">
          <input
            type="number"
            className="input"
            value={form.sortOrder}
            onChange={(e) => setForm({ ...form, sortOrder: Number(e.target.value) })}
          />
        </Field>
        <Field label="Listede gösterilsin mi?">
          <input
            type="checkbox"
            checked={form.showInList}
            onChange={(e) => setForm({ ...form, showInList: e.target.checked })}
          />
        </Field>
      </div>
      <p className="text-xs text-slate-500">Anahtar ve veri tipi kaydedildikten sonra değiştirilemez.</p>
      {error && <p className="text-sm text-red-600">{error}</p>}
      <button
        type="submit"
        disabled={mutation.isPending}
        className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
      >
        {mutation.isPending ? 'Kaydediliyor...' : 'Ekle'}
      </button>
    </form>
  )
}
