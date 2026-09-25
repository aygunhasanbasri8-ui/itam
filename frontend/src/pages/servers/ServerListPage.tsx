import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { listServers } from '../../api/servers'

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: 'Aktif',
  RETIRED: 'Hizmet Dışı',
  MAINTENANCE: 'Bakımda',
}

export function ServerListPage() {
  const [hostname, setHostname] = useState('')
  const [environment, setEnvironment] = useState('')
  const [page, setPage] = useState(0)

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

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Sunucular</h1>
        <Link to="/servers/new" className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white">
          Yeni Sunucu
        </Link>
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

      {isLoading && <p className="text-slate-500">Yükleniyor...</p>}
      {isError && <p className="text-red-600">Sunucular alınamadı.</p>}

      {data && (
        <>
          <table className="w-full overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
            <thead className="bg-slate-100 text-left text-slate-600">
              <tr>
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
                    <Link to={`/servers/${server.id}`} className="font-medium text-slate-800 hover:underline">
                      {server.hostname}
                    </Link>
                  </td>
                  <td className="px-4 py-2 text-slate-600">{server.assetName}</td>
                  <td className="px-4 py-2 text-slate-600">{server.environment}</td>
                  <td className="px-4 py-2 text-slate-600">{STATUS_LABELS[server.status] ?? server.status}</td>
                  <td className="px-4 py-2 text-slate-600">{server.responsibilityOwner?.fullName ?? '-'}</td>
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
