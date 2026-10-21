import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { downloadExport, listExportColumns } from '../api/assets'
import { extractErrorMessage } from '../api/client'

export function ExportButton({ path, params }: { path: string; params: Record<string, string | undefined> }) {
  const [open, setOpen] = useState(false)
  // null: kullanıcı henüz dokunmadı, hepsi seçili sayılır
  const [selected, setSelected] = useState<Set<string> | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const { data: columns } = useQuery({
    queryKey: ['export-columns', path],
    queryFn: () => listExportColumns(path),
    enabled: open,
    staleTime: Infinity,
  })

  const allCodes = columns?.map((column) => column.code) ?? []
  const current = selected ?? new Set(allCodes)

  function toggle(code: string) {
    const next = new Set(current)
    if (next.has(code)) {
      next.delete(code)
    } else {
      next.add(code)
    }
    setSelected(next)
  }

  async function handleDownload() {
    setBusy(true)
    setError(null)
    try {
      // Hepsi seçiliyse parametre göndermiyoruz; backend boş listeyi "tüm kolonlar" sayıyor
      const columnsParam = current.size === allCodes.length ? undefined : allCodes.filter((c) => current.has(c)).join(',')
      await downloadExport(path, { ...params, columns: columnsParam })
      setOpen(false)
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="relative">
      <button
        type="button"
        onClick={() => setOpen((value) => !value)}
        className="rounded border border-slate-300 px-4 py-2 text-sm text-slate-700 hover:bg-slate-100"
      >
        Excel'e aktar
      </button>

      {open && (
        <div className="absolute right-0 z-10 mt-2 w-64 space-y-3 rounded-lg border border-slate-200 bg-white p-4 text-sm shadow-lg">
          <div className="flex items-center justify-between">
            <span className="font-medium text-slate-700">Kolonlar</span>
            <span className="space-x-2 text-xs">
              <button type="button" className="text-slate-600 hover:underline" onClick={() => setSelected(null)}>
                Tümü
              </button>
              <button type="button" className="text-slate-600 hover:underline" onClick={() => setSelected(new Set())}>
                Hiçbiri
              </button>
            </span>
          </div>

          {!columns && <p className="text-slate-500">Yükleniyor...</p>}
          <div className="max-h-64 space-y-1 overflow-auto">
            {columns?.map((column) => (
              <label key={column.code} className="flex items-center gap-2 text-slate-600">
                <input type="checkbox" checked={current.has(column.code)} onChange={() => toggle(column.code)} />
                {column.header}
              </label>
            ))}
          </div>

          {error && <p className="text-xs text-red-600">{error}</p>}
          <button
            type="button"
            onClick={handleDownload}
            disabled={busy || current.size === 0}
            className="w-full rounded bg-slate-900 px-3 py-2 font-medium text-white disabled:opacity-50"
          >
            {busy ? 'Hazırlanıyor...' : 'İndir'}
          </button>
        </div>
      )}
    </div>
  )
}
