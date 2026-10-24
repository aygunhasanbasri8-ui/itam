import { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { downloadExport, uploadImport, type ImportReport } from '../api/assets'
import { extractErrorMessage } from '../api/client'

/**
 * Excel'den toplu ekleme. Dosya önce yalnızca kontrol ediliyor; rapor hatasızsa
 * aynı dosya ikinci adımda gerçekten yazılıyor. Hatalı satır varsa backend hiçbirini
 * yazmıyor, burada da "İçe aktar" düğmesi çıkmıyor.
 */
export function ImportButton({ path, queryKey }: { path: string; queryKey: string }) {
  const queryClient = useQueryClient()
  const [open, setOpen] = useState(false)
  const [file, setFile] = useState<File | null>(null)
  const [report, setReport] = useState<ImportReport | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function close() {
    setOpen(false)
    setFile(null)
    setReport(null)
    setError(null)
  }

  async function run(dryRun: boolean) {
    if (!file) return
    setBusy(true)
    setError(null)
    try {
      const result = await uploadImport(path, file, dryRun)
      setReport(result)
      if (!dryRun && result.created > 0) {
        await queryClient.invalidateQueries({ queryKey: [queryKey] })
      }
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  const canImport = report?.dryRun && report.errors.length === 0 && report.validRows > 0

  return (
    <div className="relative">
      <button
        type="button"
        onClick={() => (open ? close() : setOpen(true))}
        className="rounded border border-slate-300 px-4 py-2 text-sm text-slate-700 hover:bg-slate-100"
      >
        Excel'den içe aktar
      </button>

      {open && (
        <div className="absolute right-0 z-10 mt-2 w-96 space-y-3 rounded-lg border border-slate-200 bg-white p-4 text-sm shadow-lg">
          <p className="text-slate-600">
            Başlıklar dışa aktarılan dosyayla aynı. Kişi kolonlarına sicil kodu, e-posta ya da tam ad yazılabilir.{' '}
            <button
              type="button"
              className="text-slate-900 underline"
              onClick={() => downloadExport(`${path}/template`, {}).catch((err) => setError(extractErrorMessage(err)))}
            >
              Şablonu indir
            </button>
          </p>

          <input
            type="file"
            accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            onChange={(e) => {
              setFile(e.target.files?.[0] ?? null)
              setReport(null)
            }}
            className="block w-full text-xs"
          />

          {report && <ReportView report={report} />}
          {error && <p className="text-xs text-red-600">{error}</p>}

          <div className="flex gap-2">
            {report && !report.dryRun ? (
              <button type="button" onClick={close} className="w-full rounded bg-slate-900 px-3 py-2 font-medium text-white">
                Kapat
              </button>
            ) : (
              <>
                <button
                  type="button"
                  onClick={() => run(true)}
                  disabled={!file || busy}
                  className="flex-1 rounded border border-slate-300 px-3 py-2 text-slate-700 disabled:opacity-50"
                >
                  {busy && !canImport ? 'Kontrol ediliyor...' : 'Kontrol et'}
                </button>
                <button
                  type="button"
                  onClick={() => run(false)}
                  disabled={!canImport || busy}
                  className="flex-1 rounded bg-slate-900 px-3 py-2 font-medium text-white disabled:opacity-50"
                >
                  {busy && canImport ? 'Aktarılıyor...' : 'İçe aktar'}
                </button>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  )
}

function ReportView({ report }: { report: ImportReport }) {
  if (!report.dryRun && report.errors.length === 0) {
    return <p className="rounded bg-green-50 px-3 py-2 text-green-700">{report.created} kayıt eklendi.</p>
  }
  if (report.errors.length === 0) {
    return (
      <p className="rounded bg-slate-50 px-3 py-2 text-slate-700">
        {report.validRows} satır hatasız, içe aktarmaya hazır.
      </p>
    )
  }
  return (
    <div className="space-y-1">
      <p className="text-red-700">
        {report.totalRows} satırdan {report.totalRows - report.validRows} tanesinde hata var; hiçbir kayıt eklenmedi.
      </p>
      <ul className="max-h-48 space-y-0.5 overflow-auto rounded bg-red-50 px-3 py-2 text-xs text-red-700">
        {report.errors.map((e, i) => (
          <li key={i}>
            Satır {e.row}
            {e.column ? ` · ${e.column}` : ''}: {e.message}
          </li>
        ))}
      </ul>
    </div>
  )
}
