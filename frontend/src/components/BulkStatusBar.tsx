import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { bulkUpdateStatus } from '../api/assets'
import { extractErrorMessage } from '../api/client'
import { useIsAdmin } from '../auth/useIsAdmin'
import { ASSET_STATUS_LABELS, type AssetStatus } from '../types/common'
import type { Selection } from '../hooks/useSelection'

export function BulkStatusBar({ selection, queryKey }: { selection: Selection; queryKey: string }) {
  const queryClient = useQueryClient()
  const isAdmin = useIsAdmin()
  const [status, setStatus] = useState<AssetStatus>('ACTIVE')
  const [error, setError] = useState<string | null>(null)

  const mutation = useMutation({
    mutationFn: () => bulkUpdateStatus(selection.ids, status),
    onSuccess: () => {
      setError(null)
      selection.clear()
      queryClient.invalidateQueries({ queryKey: [queryKey] })
      queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
    },
    onError: (err) => setError(extractErrorMessage(err)),
  })

  if (!isAdmin || selection.ids.length === 0) {
    return null
  }

  return (
    <div className="flex flex-wrap items-center gap-3 rounded border border-slate-300 bg-slate-100 px-4 py-2 text-sm">
      <span className="font-medium text-slate-700">{selection.ids.length} kayıt seçili</span>
      <select className="rounded border border-slate-300 px-2 py-1" value={status} onChange={(e) => setStatus(e.target.value as AssetStatus)}>
        {Object.entries(ASSET_STATUS_LABELS).map(([value, label]) => (
          <option key={value} value={value}>
            {label}
          </option>
        ))}
      </select>
      <button
        type="button"
        disabled={mutation.isPending}
        onClick={() => mutation.mutate()}
        className="rounded bg-slate-900 px-3 py-1 font-medium text-white disabled:opacity-50"
      >
        Durumu güncelle
      </button>
      <button type="button" onClick={selection.clear} className="text-slate-500 hover:underline">
        Seçimi temizle
      </button>
      {error && <span className="text-red-600">{error}</span>}
    </div>
  )
}
