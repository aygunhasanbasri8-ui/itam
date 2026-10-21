import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { BulkStatusBar } from '../../components/BulkStatusBar'
import { ExportButton } from '../../components/ExportButton'
import { useSelection } from '../../hooks/useSelection'
import { Pagination } from '../../components/Pagination'
import { ASSET_STATUS_LABELS } from '../../types/common'
import { listApplications } from '../../api/applications'

export function ApplicationListPage() {
  const [vendor, setVendor] = useState('')
  const [page, setPage] = useState(0)
  const selection = useSelection()

  const { data, isLoading, isError } = useQuery({
    queryKey: ['applications', { vendor, page }],
    queryFn: () => listApplications({ vendor: vendor || undefined, page, size: 20 }),
  })

  const pageIds = data?.content.map((item) => item.id) ?? []

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Uygulama & Yazılım</h1>
        <div className="flex gap-2">
          <ExportButton path="/applications/export" params={{ vendor: vendor || undefined }} />
          <Link to="/applications/new" className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white">
            Yeni Uygulama
          </Link>
        </div>
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

      <BulkStatusBar selection={selection} queryKey="applications" />

      {isLoading && <p className="text-slate-500">Yükleniyor...</p>}
      {isError && <p className="text-red-600">Kayıtlar alınamadı.</p>}

      {data && (
        <>
          <table className="w-full overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
            <thead className="bg-slate-100 text-left text-slate-600">
              <tr>
                <th className="w-8 px-4 py-2">
                  <input
                    type="checkbox"
                    aria-label="Sayfadakilerin hepsini seç"
                    checked={selection.isAllSelected(pageIds)}
                    onChange={() => selection.toggleAll(pageIds)}
                  />
                </th>
                <th className="px-4 py-2">Asset Adı</th>
                <th className="px-4 py-2">Sürüm</th>
                <th className="px-4 py-2">Üretici</th>
                <th className="px-4 py-2">Lisans Bitiş</th>
                <th className="px-4 py-2">Sorumlu</th>
                <th className="px-4 py-2">Durum</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((app) => (
                <tr key={app.id} className="border-t border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-2">
                    <input
                      type="checkbox"
                      aria-label="Seç"
                      checked={selection.has(app.id)}
                      onChange={() => selection.toggle(app.id)}
                    />
                  </td>
                  <td className="px-4 py-2">
                    <Link to={`/applications/${app.id}`} className="font-medium text-slate-800 hover:underline">
                      {app.assetName}
                    </Link>
                  </td>
                  <td className="px-4 py-2 text-slate-600">{app.version ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{app.vendor ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{app.expiryDate ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{app.responsibilityOwner?.fullName ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{ASSET_STATUS_LABELS[app.status]}</td>
                </tr>
              ))}
              {data.content.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-4 py-6 text-center text-slate-400">
                    Kayıt bulunamadı.
                  </td>
                </tr>
              )}
            </tbody>
          </table>

          <Pagination data={data} page={page} onPageChange={setPage} />
        </>
      )}
    </div>
  )
}
