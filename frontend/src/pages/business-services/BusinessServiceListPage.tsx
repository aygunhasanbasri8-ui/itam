import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { useIsAdmin } from '../../auth/useIsAdmin'
import { BulkStatusBar } from '../../components/BulkStatusBar'
import { ExportButton } from '../../components/ExportButton'
import { OwnerFilter } from '../../components/OwnerFilter'
import { useSelection } from '../../hooks/useSelection'
import { formatCustomFieldValue, useListCustomFields } from '../../hooks/useListCustomFields'
import { Pagination } from '../../components/Pagination'
import { ASSET_STATUS_LABELS } from '../../types/common'
import { listBusinessServices } from '../../api/businessServices'

const CRITICALITY_LABELS: Record<string, string> = {
  HIGH: 'Yüksek',
  MID: 'Orta',
  LOW: 'Düşük',
}

export function BusinessServiceListPage() {
  const isAdmin = useIsAdmin()
  const [criticality, setCriticality] = useState('')
  const [ownerId, setOwnerId] = useState('')
  const [page, setPage] = useState(0)
  const selection = useSelection()
  const listFields = useListCustomFields('BUSINESS_SERVICE')

  const { data, isLoading, isError } = useQuery({
    queryKey: ['business-services', { criticality, ownerId, page }],
    queryFn: () =>
      listBusinessServices({
        criticality: criticality || undefined,
        responsibilityOwnerId: ownerId ? Number(ownerId) : undefined,
        page,
        size: 20,
      }),
  })

  const pageIds = data?.content.map((item) => item.id) ?? []

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">İş Servisleri</h1>
        <div className="flex gap-2">
          <ExportButton
            path="/business-services/export"
            params={{
              criticality: criticality || undefined,
              responsibilityOwnerId: ownerId || undefined,
            }}
          />
          {isAdmin && (
            <Link to="/business-services/new" className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white">
              Yeni İş Servisi
            </Link>
          )}
        </div>
      </div>

      <select
        className="input max-w-xs"
        value={criticality}
        onChange={(e) => {
          setPage(0)
          setCriticality(e.target.value)
        }}
      >
        <option value="">Tüm kritiklik seviyeleri</option>
        <option value="HIGH">Yüksek</option>
        <option value="MID">Orta</option>
        <option value="LOW">Düşük</option>
      </select>

      <OwnerFilter
        value={ownerId}
        onChange={(value) => {
          setPage(0)
          setOwnerId(value)
        }}
      />

      <BulkStatusBar selection={selection} queryKey="business-services" />

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
                <th className="px-4 py-2">Kritiklik</th>
                <th className="px-4 py-2">SLA</th>
                <th className="px-4 py-2">Servis Sahibi</th>
                <th className="px-4 py-2">Sorumlu</th>
                <th className="px-4 py-2">Durum</th>
                {listFields.map((definition) => (
                  <th key={definition.id} className="px-4 py-2">
                    {definition.label}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {data.content.map((service) => (
                <tr key={service.id} className="border-t border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-2">
                    <input
                      type="checkbox"
                      aria-label="Seç"
                      checked={selection.has(service.id)}
                      onChange={() => selection.toggle(service.id)}
                    />
                  </td>
                  <td className="px-4 py-2">
                    <Link
                      to={`/business-services/${service.id}`}
                      className="font-medium text-slate-800 hover:underline"
                    >
                      {service.assetName}
                    </Link>
                  </td>
                  <td className="px-4 py-2 text-slate-600">
                    {CRITICALITY_LABELS[service.criticality] ?? service.criticality}
                  </td>
                  <td className="px-4 py-2 text-slate-600">{service.sla ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{service.serviceOwner?.fullName ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{service.responsibilityOwner?.fullName ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{ASSET_STATUS_LABELS[service.status]}</td>
                  {listFields.map((definition) => (
                    <td key={definition.id} className="px-4 py-2 text-slate-600">
                      {formatCustomFieldValue(definition, service.customFields[definition.fieldKey])}
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
