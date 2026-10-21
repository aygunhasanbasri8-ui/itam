import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { createBusinessService, getBusinessService, updateBusinessService } from '../../api/businessServices'
import { listActivePeople } from '../../api/people'
import { extractErrorMessage } from '../../api/client'
import { Field } from '../../components/Field'
import { DependencyPanel } from '../../components/DependencyPanel'
import { AuditTrailPanel } from '../../components/AuditTrailPanel'
import type { BusinessServiceAssetRequest, ServiceCriticality } from '../../types/businessService'

const EMPTY_FORM: BusinessServiceAssetRequest = { assetName: '' }

export function BusinessServiceFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState<BusinessServiceAssetRequest>(EMPTY_FORM)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

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

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      if (isEdit) {
        await updateBusinessService(Number(id), form)
      } else {
        await createBusinessService(form)
      }
      navigate('/business-services')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-2xl space-y-4">
      <h1 className="text-xl font-semibold text-slate-800">{isEdit ? 'İş Servisini Düzenle' : 'Yeni İş Servisi'}</h1>

      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg border border-slate-200 bg-white p-6">
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

        {error && <p className="text-sm text-red-600">{error}</p>}

        <div className="flex gap-3">
          <button
            type="submit"
            disabled={submitting}
            className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
          >
            {submitting ? 'Kaydediliyor...' : 'Kaydet'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/business-services')}
            className="rounded border border-slate-300 px-4 py-2 text-sm text-slate-600"
          >
            Vazgeç
          </button>
        </div>
      </form>

      {isEdit && (
        <>
          <DependencyPanel assetId={Number(id)} />
          <AuditTrailPanel assetId={Number(id)} />
        </>
      )}
    </div>
  )
}
