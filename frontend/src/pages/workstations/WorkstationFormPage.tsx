import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { createWorkstation, getWorkstation, updateWorkstation } from '../../api/workstations'
import { listActivePeople } from '../../api/people'
import { listActiveDepartments } from '../../api/departments'
import { extractErrorMessage } from '../../api/client'
import type { WorkstationAssetRequest } from '../../types/workstation'
import { Field } from '../../components/Field'
import { DependencyPanel } from '../../components/DependencyPanel'
import { AuditTrailPanel } from '../../components/AuditTrailPanel'

const EMPTY_FORM: WorkstationAssetRequest = { assetName: '', serialNumber: '', notes: '' }

export function WorkstationFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState<WorkstationAssetRequest>(EMPTY_FORM)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const { data: people } = useQuery({ queryKey: ['people', 'active'], queryFn: listActivePeople })
  const { data: departments } = useQuery({ queryKey: ['departments', 'active'], queryFn: listActiveDepartments })

  const { data: existing, isLoading } = useQuery({
    queryKey: ['workstations', id],
    queryFn: () => getWorkstation(Number(id)),
    enabled: isEdit,
  })

  useEffect(() => {
    if (existing) {
      setForm({
        assetName: existing.assetName,
        serialNumber: existing.serialNumber,
        notes: existing.notes ?? '',
        model: existing.model ?? '',
        cpu: existing.cpu ?? '',
        ramGb: existing.ramGb ?? undefined,
        diskGb: existing.diskGb ?? undefined,
        responsibilityOwnerId: existing.responsibilityOwner?.id ?? null,
        assignedPersonId: existing.assignedPerson?.id ?? null,
        departmentId: existing.department?.id ?? null,
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
        await updateWorkstation(Number(id), form)
      } else {
        await createWorkstation(form)
      }
      navigate('/workstations')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-2xl space-y-4">
      <h1 className="text-xl font-semibold text-slate-800">{isEdit ? 'İş İstasyonunu Düzenle' : 'Yeni İş İstasyonu'}</h1>

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

          <Field label="Seri Numarası" required>
            <input
              className="input"
              value={form.serialNumber}
              onChange={(e) => setForm({ ...form, serialNumber: e.target.value })}
              required
            />
          </Field>

          <Field label="Model">
            <input className="input" value={form.model ?? ''} onChange={(e) => setForm({ ...form, model: e.target.value })} />
          </Field>

          <Field label="İşlemci">
            <input className="input" value={form.cpu ?? ''} onChange={(e) => setForm({ ...form, cpu: e.target.value })} />
          </Field>

          <Field label="RAM (GB)">
            <input
              type="number"
              className="input"
              value={form.ramGb ?? ''}
              onChange={(e) => setForm({ ...form, ramGb: e.target.value ? Number(e.target.value) : undefined })}
            />
          </Field>

          <Field label="Disk (GB)">
            <input
              type="number"
              className="input"
              value={form.diskGb ?? ''}
              onChange={(e) => setForm({ ...form, diskGb: e.target.value ? Number(e.target.value) : undefined })}
            />
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

          <Field label="Zimmetli Kişi">
            <select
              className="input"
              value={form.assignedPersonId ?? ''}
              onChange={(e) => setForm({ ...form, assignedPersonId: e.target.value ? Number(e.target.value) : null })}
            >
              <option value="">Yok</option>
              {people?.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.fullName}
                </option>
              ))}
            </select>
          </Field>

          <Field label="Departman">
            <select
              className="input"
              value={form.departmentId ?? ''}
              onChange={(e) => setForm({ ...form, departmentId: e.target.value ? Number(e.target.value) : null })}
            >
              <option value="">Yok</option>
              {departments?.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name}
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
            onClick={() => navigate('/workstations')}
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
