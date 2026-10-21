import { useState } from 'react'
import { downloadExport } from '../api/assets'
import { extractErrorMessage } from '../api/client'

export function ExportButton({ path, params }: { path: string; params: Record<string, string | undefined> }) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleClick() {
    setBusy(true)
    setError(null)
    try {
      await downloadExport(path, params)
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <span className="flex items-center gap-2">
      {error && <span className="text-xs text-red-600">{error}</span>}
      <button
        type="button"
        onClick={handleClick}
        disabled={busy}
        className="rounded border border-slate-300 px-4 py-2 text-sm text-slate-700 hover:bg-slate-100 disabled:opacity-50"
      >
        {busy ? 'Hazırlanıyor...' : "Excel'e aktar"}
      </button>
    </span>
  )
}
