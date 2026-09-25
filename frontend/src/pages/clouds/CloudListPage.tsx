import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { listClouds } from '../../api/clouds'

export function CloudListPage() {
  const [provider, setProvider] = useState('')
  const [page, setPage] = useState(0)

  const { data, isLoading, isError } = useQuery({
    queryKey: ['clouds', { provider, page }],
    queryFn: () => listClouds({ provider: provider || undefined, page, size: 20 }),
  })

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Cloud Varlıkları</h1>
        <Link to="/clouds/new" className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white">
          Yeni Cloud Varlığı
        </Link>
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

      {isLoading && <p className="text-slate-500">Yükleniyor...</p>}
      {isError && <p className="text-red-600">Kayıtlar alınamadı.</p>}

      {data && (
        <>
          <table className="w-full overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
            <thead className="bg-slate-100 text-left text-slate-600">
              <tr>
                <th className="px-4 py-2">Asset Adı</th>
                <th className="px-4 py-2">Sağlayıcı</th>
                <th className="px-4 py-2">Instance Tipi</th>
                <th className="px-4 py-2">Bölge</th>
                <th className="px-4 py-2">Sorumlu</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((cloud) => (
                <tr key={cloud.id} className="border-t border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-2">
                    <Link to={`/clouds/${cloud.id}`} className="font-medium text-slate-800 hover:underline">
                      {cloud.assetName}
                    </Link>
                  </td>
                  <td className="px-4 py-2 text-slate-600">{cloud.cloudProvider}</td>
                  <td className="px-4 py-2 text-slate-600">{cloud.instanceType ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{cloud.region ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{cloud.responsibilityOwner?.fullName ?? '-'}</td>
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
