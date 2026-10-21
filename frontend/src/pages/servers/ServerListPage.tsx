import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { BulkStatusBar } from '../../components/BulkStatusBar'
import { ExportButton } from '../../components/ExportButton'
import { useSelection } from '../../hooks/useSelection'
import { Pagination } from '../../components/Pagination'
import { listServers } from '../../api/servers'
import { ASSET_STATUS_LABELS } from '../../types/common'

export function ServerListPage() {
  const [hostname, setHostname] = useState('')
  const [environment, setEnvironment] = useState('')
  const [page, setPage] = useState(0)
  const selection = useSelection()

  const { data, isLoading, isError } = useQuery({
    queryKey: ['servers', { hostname, environment, page }],
    queryFn: () =>
      listServers({
        hostname: hostname || undefined,
        environment: environment || undefined,
        page,
        size: 20,
      }),
  })

  const pageIds = data?.content.map((item) => item.id) ?? []

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Sunucular</h1>
        <div className="flex gap-2">
          <ExportButton path="/servers/export" params={{ hostname: hostname || undefined, environment: environment || undefined }} />
          <Link to="/servers/new" className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white">
            Yeni Sunucu
          </Link>
        </div>
      </div>

      <div className="flex gap-3">
        <input
          placeholder="Hostname ara..."
          className="rounded border border-slate-300 px-3 py-2 text-sm"
          value={hostname}
          onChange={(e) => {
            setPage(0)
            setHostname(e.target.value)
          }}
        />
        <select
          className="rounded border border-slate-300 px-3 py-2 text-sm"
          value={environment}
          onChange={(e) => {
            setPage(0)
            setEnvironment(e.target.value)
          }}
        >
          <option value="">Tüm ortamlar</option>
          <option value="DEV">DEV</option>
          <option value="TEST">TEST</option>
          <option value="UAT">UAT</option>
          <option value="PROD">PROD</option>
        </select>
      </div>

      <BulkStatusBar selection={selection} queryKey="servers" />

      {isLoading && <p className="text-slate-500">Yükleniyor...</p>}
      {isError && <p className="text-red-600">Sunucular alınamadı.</p>}

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
                <th className="px-4 py-2">Hostname</th>
                <th className="px-4 py-2">Asset Adı</th>
                <th className="px-4 py-2">Ortam</th>
                <th className="px-4 py-2">Durum</th>
                <th className="px-4 py-2">Sorumlu</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((server) => (
                <tr key={server.id} className="border-t border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-2">
                    <input
                      type="checkbox"
                      aria-label="Seç"
                      checked={selection.has(server.id)}
                      onChange={() => selection.toggle(server.id)}
                    />
                  </td>
                  <td className="px-4 py-2">
                    <Link to={`/servers/${server.id}`} className="font-medium text-slate-800 hover:underline">
                      {server.hostname}
                    </Link>
                  </td>
                  <td className="px-4 py-2 text-slate-600">{server.assetName}</td>
                  <td className="px-4 py-2 text-slate-600">{server.environment}</td>
                  <td className="px-4 py-2 text-slate-600">{ASSET_STATUS_LABELS[server.status] ?? server.status}</td>
                  <td className="px-4 py-2 text-slate-600">{server.responsibilityOwner?.fullName ?? '-'}</td>
                </tr>
              ))}
              {data.content.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-4 py-6 text-center text-slate-400">
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
