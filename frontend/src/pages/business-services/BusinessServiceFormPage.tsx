import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { createBusinessService, getBusinessService, updateBusinessService } from '../../api/businessServices'
import { listActivePeople } from '../../api/people'
import { AssetFormShell } from '../../components/AssetFormShell'
import { Field } from '../../components/Field'
import type { BusinessServiceAssetRequest, ServiceCriticality } from '../../types/businessService'

const EMPTY_FORM: BusinessServiceAssetRequest = { assetName: '' }

export function BusinessServiceFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)

  const [form, setForm] = useState<BusinessServiceAssetRequest>(EMPTY_FORM)

  const { data: people } = useQuery({ queryKey: ['people', 'active'], queryFn: listActivePeople })

  const { data: existing, isLoading } = useQuery({
    queryKey: ['business-services', id],
    queryFn: () => getBusinessService(Number(id)),
    enabled: isEdit,
  })

  useEffect(() => {
    if (existing) {
      setForm({
        assetName: existing.assetName,
        notes: existing.notes ?? '',
        criticality: existing.criticality,
        sla: existing.sla ?? '',
        responsibilityOwnerId: existing.responsibilityOwner?.id ?? null,
        serviceOwnerId: existing.serviceOwner?.id ?? null,
        customFields: existing.customFields,
      })
    }
  }, [existing])

  if (isEdit && isLoading) {
    return <p className="text-slate-500">Yükleniyor...</p>
  }

  return (
    <AssetFormShell
      title={isEdit ? 'İş Servisini Düzenle' : 'Yeni İş Servisi'}
      assetTypeCode="BUSINESS_SERVICE"
      listPath="/business-services"
      assetId={isEdit ? Number(id) : null}
      customFields={form.customFields}
      onCustomFieldsChange={(customFields) => setForm({ ...form, customFields })}
      onSave={() => (isEdit ? updateBusinessService(Number(id), form) : createBusinessService(form))}
    >
      <div className="grid grid-cols-2 gap-4">
        <Field label="Asset Adı" required>
          <input
            className="input"
            value={form.assetName}
            onChange={(e) => setForm({ ...form, assetName: e.target.value })}
            required
          />
        </Field>

        <Field label="Kritiklik">
          <select
            className="input"
            value={form.criticality ?? ''}
            onChange={(e) => setForm({ ...form, criticality: (e.target.value || null) as ServiceCriticality | null })}
          >
            <option value="">Orta (varsayılan)</option>
            <option value="HIGH">Yüksek</option>
            <option value="MID">Orta</option>
            <option value="LOW">Düşük</option>
          </select>
        </Field>

        <Field label="SLA">
          <input className="input" value={form.sla ?? ''} onChange={(e) => setForm({ ...form, sla: e.target.value })} />
        </Field>

        <Field label="Sorumlu Kişi">
          <select
            className="input"
            value={form.responsibilityOwnerId ?? ''}
            onChange={(e) =>
              setForm({ ...form, responsibilityOwnerId: e.target.value ? Number(e.target.value) : null })
            }
          >
            <option value="">FIRMA (varsayılan)</option>
            {people?.map((p) => (
              <option key={p.id} value={p.id}>
                {p.fullName}
              </option>
            ))}
          </select>
        </Field>

        <Field label="Servis Sahibi">
          <select
            className="input"
            value={form.serviceOwnerId ?? ''}
            onChange={(e) => setForm({ ...form, serviceOwnerId: e.target.value ? Number(e.target.value) : null })}
          >
            <option value="">Yok</option>
            {people?.map((p) => (
              <option key={p.id} value={p.id}>
                {p.fullName}
              </option>
            ))}
          </select>
        </Field>
      </div>

      <Field label="Notlar">
        <textarea
          className="input"
          rows={3}
          value={form.notes ?? ''}
          onChange={(e) => setForm({ ...form, notes: e.target.value })}
        />
      </Field>
    </AssetFormShell>
  )
}
