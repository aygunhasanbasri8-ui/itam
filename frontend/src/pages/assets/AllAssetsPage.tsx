import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { listAllAssets } from '../../api/assetSearch'
import { BulkStatusBar } from '../../components/BulkStatusBar'
import { OwnerFilter } from '../../components/OwnerFilter'
import { Pagination } from '../../components/Pagination'
import { useSelection } from '../../hooks/useSelection'
import { useAssetTypes } from '../../hooks/useAssetTypeLabels'
import { ASSET_TYPE_ROUTES } from '../../types/assetType'
import { ASSET_STATUS_LABELS } from '../../types/common'

export function AllAssetsPage() {
  const { labels: assetTypeLabels, active: activeTypes } = useAssetTypes()
  const [q, setQ] = useState('')
  const [assetType, setAssetType] = useState('')
  const [status, setStatus] = useState('')
  const [ownerId, setOwnerId] = useState('')
  const [page, setPage] = useState(0)
  const selection = useSelection()

  const { data, isLoading, isError } = useQuery({
    queryKey: ['assets', { q, assetType, status, ownerId, page }],
    queryFn: () =>
      listAllAssets({
        q: q || undefined,
        assetType: assetType || undefined,
        status: status || undefined,
        responsibilityOwnerId: ownerId ? Number(ownerId) : undefined,
        page,
        size: 20,
      }),
  })

  const pageIds = data?.content.map((item) => item.id) ?? []

  // Her filtre değişiminde ilk sayfaya dönmek için ortak yardımcı
  const withReset = (setter: (value: string) => void) => (value: string) => {
    setPage(0)
    setter(value)
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-slate-800">Tüm Varlıklar</h1>

      <div className="flex flex-wrap items-center gap-3">
        <input
          placeholder="Ada göre ara..."
          className="rounded border border-slate-300 px-3 py-2 text-sm"
          value={q}
          onChange={(e) => withReset(setQ)(e.target.value)}
        />
        <select
          className="rounded border border-slate-300 px-3 py-2 text-sm"
          value={assetType}
          onChange={(e) => withReset(setAssetType)(e.target.value)}
        >
          <option value="">Tüm tipler</option>
          {activeTypes.map(({ code, label }) => (
            <option key={code} value={code}>
              {label}
            </option>
          ))}
        </select>
        <select
          className="rounded border border-slate-300 px-3 py-2 text-sm"
          value={status}
          onChange={(e) => withReset(setStatus)(e.target.value)}
        >
          <option value="">Tüm durumlar</option>
          {Object.entries(ASSET_STATUS_LABELS).map(([code, label]) => (
            <option key={code} value={code}>
              {label}
            </option>
          ))}
        </select>
        <OwnerFilter value={ownerId} onChange={withReset(setOwnerId)} />
      </div>

      <BulkStatusBar selection={selection} queryKey="assets" />

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
                <th className="px-4 py-2">Ad</th>
                <th className="px-4 py-2">Tip</th>
                <th className="px-4 py-2">Durum</th>
                <th className="px-4 py-2">Sorumlu</th>
                <th className="px-4 py-2">Eklenme</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((asset) => (
                <tr key={asset.id} className="border-t border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-2">
                    <input
                      type="checkbox"
                      aria-label="Seç"
                      checked={selection.has(asset.id)}
                      onChange={() => selection.toggle(asset.id)}
                    />
                  </td>
                  <td className="px-4 py-2">
                    <Link
                      to={`${ASSET_TYPE_ROUTES[asset.assetTypeCode]}/${asset.id}`}
                      className="font-medium text-slate-800 hover:underline"
                    >
                      {asset.assetName}
                    </Link>
                  </td>
                  <td className="px-4 py-2 text-slate-600">{assetTypeLabels[asset.assetTypeCode]}</td>
                  <td className="px-4 py-2 text-slate-600">{ASSET_STATUS_LABELS[asset.status]}</td>
                  <td className="px-4 py-2 text-slate-600">{asset.responsibilityOwner?.fullName ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{new Date(asset.createdAt).toLocaleDateString('tr-TR')}</td>
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
