import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { BulkStatusBar } from '../../components/BulkStatusBar'
import { ExportButton } from '../../components/ExportButton'
import { useSelection } from '../../hooks/useSelection'
import { Pagination } from '../../components/Pagination'
import { ASSET_STATUS_LABELS } from '../../types/common'
import { listClouds } from '../../api/clouds'

export function CloudListPage() {
  const [provider, setProvider] = useState('')
  const [page, setPage] = useState(0)
  const selection = useSelection()

  const { data, isLoading, isError } = useQuery({
    queryKey: ['clouds', { provider, page }],
    queryFn: () => listClouds({ provider: provider || undefined, page, size: 20 }),
  })

  const pageIds = data?.content.map((item) => item.id) ?? []

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Cloud Varlıkları</h1>
        <div className="flex gap-2">
          <ExportButton path="/cloud-assets/export" params={{ provider: provider || undefined }} />
          <Link to="/clouds/new" className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white">
            Yeni Cloud Varlığı
          </Link>
        </div>
      </div>

      <select
        className="input max-w-xs"
        value={provider}
        onChange={(e) => {
          setPage(0)
          setProvider(e.target.value)
        }}
      >
        <option value="">Tüm sağlayıcılar</option>
        <option value="AWS">AWS</option>
        <option value="AZURE">AZURE</option>
        <option value="GCP">GCP</option>
        <option value="OTHER">OTHER</option>
      </select>

      <BulkStatusBar selection={selection} queryKey="clouds" />

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
                <th className="px-4 py-2">Sağlayıcı</th>
                <th className="px-4 py-2">Instance Tipi</th>
                <th className="px-4 py-2">Bölge</th>
                <th className="px-4 py-2">Sorumlu</th>
                <th className="px-4 py-2">Durum</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((cloud) => (
                <tr key={cloud.id} className="border-t border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-2">
                    <input
                      type="checkbox"
                      aria-label="Seç"
                      checked={selection.has(cloud.id)}
                      onChange={() => selection.toggle(cloud.id)}
                    />
                  </td>
                  <td className="px-4 py-2">
                    <Link to={`/clouds/${cloud.id}`} className="font-medium text-slate-800 hover:underline">
                      {cloud.assetName}
                    </Link>
                  </td>
                  <td className="px-4 py-2 text-slate-600">{cloud.cloudProvider}</td>
                  <td className="px-4 py-2 text-slate-600">{cloud.instanceType ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{cloud.region ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{cloud.responsibilityOwner?.fullName ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{ASSET_STATUS_LABELS[cloud.status]}</td>
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
