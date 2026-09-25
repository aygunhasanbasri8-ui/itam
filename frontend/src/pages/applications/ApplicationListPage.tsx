import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { listApplications } from '../../api/applications'

export function ApplicationListPage() {
  const [vendor, setVendor] = useState('')
  const [page, setPage] = useState(0)

  const { data, isLoading, isError } = useQuery({
    queryKey: ['applications', { vendor, page }],
    queryFn: () => listApplications({ vendor: vendor || undefined, page, size: 20 }),
  })

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Uygulama & Yazılım</h1>
        <Link to="/applications/new" className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white">
          Yeni Uygulama
        </Link>
      </div>

      <input
        placeholder="Üretici ara..."
        className="input max-w-xs"
        value={vendor}
        onChange={(e) => {
          setPage(0)
          setVendor(e.target.value)
        }}
      />

      {isLoading && <p className="text-slate-500">Yükleniyor...</p>}
      {isError && <p className="text-red-600">Kayıtlar alınamadı.</p>}

      {data && (
        <>
          <table className="w-full overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
            <thead className="bg-slate-100 text-left text-slate-600">
              <tr>
                <th className="px-4 py-2">Asset Adı</th>
                <th className="px-4 py-2">Sürüm</th>
                <th className="px-4 py-2">Üretici</th>
                <th className="px-4 py-2">Lisans Bitiş</th>
                <th className="px-4 py-2">Sorumlu</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((app) => (
                <tr key={app.id} className="border-t border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-2">
                    <Link to={`/applications/${app.id}`} className="font-medium text-slate-800 hover:underline">
                      {app.assetName}
                    </Link>
                  </td>
                  <td className="px-4 py-2 text-slate-600">{app.version ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{app.vendor ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{app.expiryDate ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{app.responsibilityOwner?.fullName ?? '-'}</td>
                </tr>
              ))}
              {data.content.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-4 py-6 text-center text-slate-400">
                    Kayıt bulunamadı.
                  </td>
                </tr>
              )}
            </tbody>
          </table>

          <div className="flex items-center justify-between text-sm text-slate-500">
            <span>
              Sayfa {data.page + 1} / {Math.max(data.totalPages, 1)} — Toplam {data.totalElements} kayıt
            </span>
            <div className="flex gap-2">
              <button
                type="button"
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
                className="rounded border border-slate-300 px-3 py-1 disabled:opacity-40"
              >
                Önceki
              </button>
              <button
                type="button"
                disabled={data.last}
                onClick={() => setPage((p) => p + 1)}
                className="rounded border border-slate-300 px-3 py-1 disabled:opacity-40"
              >
                Sonraki
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  )
}
