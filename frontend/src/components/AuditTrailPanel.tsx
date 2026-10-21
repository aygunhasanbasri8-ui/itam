import { useQuery } from '@tanstack/react-query'
import { getAuditTrail } from '../api/audit'

const FIELD_LABELS: Record<string, string> = {
  assetName: 'Asset adı',
  status: 'Durum',
  notes: 'Notlar',
  deleted: 'Silindi',
  responsibilityOwnerId: 'Sorumlu kişi (id)',
  assetType: 'Asset tipi',
}

const CUSTOM_FIELD_PREFIX = 'customFields.'

function fieldLabel(field: string): string {
  if (field.startsWith(CUSTOM_FIELD_PREFIX)) {
    return `Özel alan: ${field.slice(CUSTOM_FIELD_PREFIX.length)}`
  }
  return FIELD_LABELS[field] ?? field
}

const REVISION_LABELS: Record<string, string> = {
  ADD: 'Oluşturuldu',
  MOD: 'Güncellendi',
  DEL: 'Silindi',
}

export function AuditTrailPanel({ assetId }: { assetId: number }) {
  const { data: revisions, isLoading, isError } = useQuery({
    queryKey: ['audit-trail', assetId],
    queryFn: () => getAuditTrail(assetId),
  })

  return (
    <section className="space-y-3 rounded-lg border border-slate-200 bg-white p-6 text-sm">
      <h2 className="text-lg font-semibold text-slate-800">Değişiklik Geçmişi</h2>

      {isLoading && <p className="text-slate-500">Yükleniyor...</p>}
      {isError && <p className="text-red-600">Değişiklik geçmişi alınamadı.</p>}
      {revisions?.length === 0 && <p className="text-slate-400">Kayıtlı revizyon yok.</p>}

      <ol className="space-y-3">
        {/* En yeni revizyon üstte olsun; backend kronolojik sırada döndürüyor */}
        {revisions
          ?.slice()
          .reverse()
          .map((revision) => (
            <li key={revision.revision} className="rounded border border-slate-200 p-3">
              <div className="flex justify-between text-xs text-slate-500">
                <span>
                  <span className="font-medium text-slate-700">
                    {REVISION_LABELS[revision.revisionType] ?? revision.revisionType}
                  </span>
                  {revision.username && <> · {revision.username}</>}
                </span>
                <span>{new Date(revision.timestamp).toLocaleString('tr-TR')}</span>
              </div>
              {revision.changes.length > 0 && (
                <table className="mt-2 w-full text-left">
                  <tbody>
                    {revision.changes.map((change) => (
                      <tr key={change.field} className="border-t border-slate-100">
                        <td className="w-40 py-1 text-slate-500">{fieldLabel(change.field)}</td>
                        <td className="py-1 text-slate-400 line-through">{change.oldValue ?? '—'}</td>
                        <td className="py-1 text-slate-700">{change.newValue ?? '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </li>
          ))}
      </ol>
    </section>
  )
}
