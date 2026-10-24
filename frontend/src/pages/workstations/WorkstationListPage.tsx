import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { useIsAdmin } from '../../auth/useIsAdmin'
import { BulkStatusBar } from '../../components/BulkStatusBar'
import { ExportButton } from '../../components/ExportButton'
import { ImportButton } from '../../components/ImportButton'
import { OwnerFilter } from '../../components/OwnerFilter'
import { useSelection } from '../../hooks/useSelection'
import { formatCustomFieldValue, useListCustomFields } from '../../hooks/useListCustomFields'
import { Pagination } from '../../components/Pagination'
import { ASSET_STATUS_LABELS } from '../../types/common'
import { listWorkstations } from '../../api/workstations'

export function WorkstationListPage() {
  const isAdmin = useIsAdmin()
  const [serialNumber, setSerialNumber] = useState('')
  const [ownerId, setOwnerId] = useState('')
  const [page, setPage] = useState(0)
  const selection = useSelection()
  const listFields = useListCustomFields('WORKSTATION')

  const { data, isLoading, isError } = useQuery({
    queryKey: ['workstations', { serialNumber, ownerId, page }],
    queryFn: () =>
      listWorkstations({
        serialNumber: serialNumber || undefined,
        responsibilityOwnerId: ownerId ? Number(ownerId) : undefined,
        page,
        size: 20,
      }),
  })

  const pageIds = data?.content.map((item) => item.id) ?? []

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">İş İstasyonları</h1>
        <div className="flex gap-2">
          <ExportButton
            path="/workstations/export"
            params={{
              serialNumber: serialNumber || undefined,
              responsibilityOwnerId: ownerId || undefined,
            }}
          />
          {isAdmin && <ImportButton path="/workstations/import" queryKey="workstations" />}
          {isAdmin && (
            <Link to="/workstations/new" className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white">
              Yeni İş İstasyonu
            </Link>
          )}
        </div>
      </div>

      <input
        placeholder="Seri numarası ara..."
        className="input max-w-xs"
        value={serialNumber}
        onChange={(e) => {
          setPage(0)
          setSerialNumber(e.target.value)
        }}
      />

      <OwnerFilter
        value={ownerId}
        onChange={(value) => {
          setPage(0)
          setOwnerId(value)
        }}
      />

      <BulkStatusBar selection={selection} queryKey="workstations" />

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
                <th className="px-4 py-2">Seri No</th>
                <th className="px-4 py-2">Asset Adı</th>
                <th className="px-4 py-2">Model</th>
                <th className="px-4 py-2">Departman</th>
                <th className="px-4 py-2">Zimmetli Kişi</th>
                <th className="px-4 py-2">Durum</th>
                {listFields.map((definition) => (
                  <th key={definition.id} className="px-4 py-2">
                    {definition.label}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {data.content.map((ws) => (
                <tr key={ws.id} className="border-t border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-2">
                    <input
                      type="checkbox"
                      aria-label="Seç"
                      checked={selection.has(ws.id)}
                      onChange={() => selection.toggle(ws.id)}
                    />
                  </td>
                  <td className="px-4 py-2">
                    <Link to={`/workstations/${ws.id}`} className="font-medium text-slate-800 hover:underline">
                      {ws.serialNumber}
                    </Link>
                  </td>
                  <td className="px-4 py-2 text-slate-600">{ws.assetName}</td>
                  <td className="px-4 py-2 text-slate-600">{ws.model ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{ws.department?.name ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{ws.assignedPerson?.fullName ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{ASSET_STATUS_LABELS[ws.status]}</td>
                  {listFields.map((definition) => (
                    <td key={definition.id} className="px-4 py-2 text-slate-600">
                      {formatCustomFieldValue(definition, ws.customFields[definition.fieldKey])}
                    </td>
                  ))}
                </tr>
              ))}
              {data.content.length === 0 && (
                <tr>
                  <td colSpan={7 + listFields.length} className="px-4 py-6 text-center text-slate-400">
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
