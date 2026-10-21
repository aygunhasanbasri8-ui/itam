import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { listAssetTypes, updateAssetType, type AssetTypeDefinition } from '../../api/assetTypes'
import { extractErrorMessage } from '../../api/client'
import { useIsAdmin } from '../../auth/useIsAdmin'

export function AssetTypesPage() {
  const canEdit = useIsAdmin()
  const { data, isLoading, isError } = useQuery({ queryKey: ['asset-types'], queryFn: listAssetTypes })

  return (
    <div className="max-w-4xl space-y-4">
      <h1 className="text-xl font-semibold text-slate-800">Asset Tipleri</h1>
      <p className="text-sm text-slate-500">
        Tip kodları sabittir; her tipin kendi tablosu ve ekranı var. Burada yalnızca görünen ad, ikon, açıklama ve
        sıra düzenlenir.
      </p>

      {isLoading && <p className="text-slate-500">Yükleniyor...</p>}
      {isError && <p className="text-red-600">Asset tipleri alınamadı.</p>}

      {data && (
        <table className="w-full overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
          <thead className="bg-slate-100 text-left text-slate-600">
            <tr>
              <th className="px-4 py-2">Kod</th>
              <th className="px-4 py-2">Görünen Ad</th>
              <th className="px-4 py-2">İkon</th>
              <th className="px-4 py-2">Açıklama</th>
              <th className="px-4 py-2">Sıra</th>
              {canEdit && <th className="px-4 py-2" />}
            </tr>
          </thead>
          <tbody>
            {data.map((type) => (
              <AssetTypeRow key={type.code} type={type} canEdit={canEdit} />
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}

function AssetTypeRow({ type, canEdit }: { type: AssetTypeDefinition; canEdit: boolean }) {
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState(false)
  const [draft, setDraft] = useState({
    displayName: type.displayName,
    icon: type.icon,
    description: type.description ?? '',
    sortOrder: type.sortOrder,
  })
  const [error, setError] = useState<string | null>(null)

  const mutation = useMutation({
    mutationFn: () => updateAssetType(type.code, { ...draft, description: draft.description || null }),
    onSuccess: () => {
      setEditing(false)
      setError(null)
      queryClient.invalidateQueries({ queryKey: ['asset-types'] })
    },
    onError: (err) => setError(extractErrorMessage(err)),
  })

  if (!editing) {
    return (
      <tr className="border-t border-slate-100">
        <td className="px-4 py-2 font-mono text-xs text-slate-600">{type.code}</td>
        <td className="px-4 py-2 text-slate-700">{type.displayName}</td>
        <td className="px-4 py-2 font-mono text-xs text-slate-600">{type.icon}</td>
        <td className="px-4 py-2 text-slate-600">{type.description ?? '-'}</td>
        <td className="px-4 py-2 text-slate-600">{type.sortOrder}</td>
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
      <td className="px-4 py-2 font-mono text-xs text-slate-600">{type.code}</td>
      <td className="px-4 py-2">
        <input
          className="input"
          maxLength={100}
          value={draft.displayName}
          onChange={(e) => setDraft({ ...draft, displayName: e.target.value })}
        />
        {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
      </td>
      <td className="px-4 py-2">
        <input
          className="input font-mono"
          maxLength={60}
          value={draft.icon}
          onChange={(e) => setDraft({ ...draft, icon: e.target.value })}
        />
      </td>
      <td className="px-4 py-2">
        <input
          className="input"
          maxLength={255}
          value={draft.description}
          onChange={(e) => setDraft({ ...draft, description: e.target.value })}
        />
      </td>
      <td className="px-4 py-2">
        <input
          type="number"
          className="input w-20"
          value={draft.sortOrder}
          onChange={(e) => setDraft({ ...draft, sortOrder: Number(e.target.value) })}
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
