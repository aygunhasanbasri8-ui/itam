import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { createApplication, getApplication, updateApplication } from '../../api/applications'
import { listActivePeople } from '../../api/people'
import { extractErrorMessage } from '../../api/client'
import { Field } from '../../components/Field'
import type { ApplicationAssetRequest } from '../../types/application'

const EMPTY_FORM: ApplicationAssetRequest = { assetName: '' }

export function ApplicationFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState<ApplicationAssetRequest>(EMPTY_FORM)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const { data: people } = useQuery({ queryKey: ['people', 'active'], queryFn: listActivePeople })

  const { data: existing, isLoading } = useQuery({
    queryKey: ['applications', id],
    queryFn: () => getApplication(Number(id)),
    enabled: isEdit,
  })

  useEffect(() => {
    if (existing) {
      setForm({
        assetName: existing.assetName,
        notes: existing.notes ?? '',
        version: existing.version ?? '',
        licenseType: existing.licenseType ?? '',
        licenseCount: existing.licenseCount ?? undefined,
        vendor: existing.vendor ?? '',
        expiryDate: existing.expiryDate ?? '',
        installPath: existing.installPath ?? '',
        responsibilityOwnerId: existing.responsibilityOwner?.id ?? null,
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
        await updateApplication(Number(id), form)
      } else {
        await createApplication(form)
      }
      navigate('/applications')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-2xl space-y-4">
      <h1 className="text-xl font-semibold text-slate-800">{isEdit ? 'Uygulamayı Düzenle' : 'Yeni Uygulama'}</h1>

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

          <Field label="Sürüm">
            <input className="input" value={form.version ?? ''} onChange={(e) => setForm({ ...form, version: e.target.value })} />
          </Field>

          <Field label="Üretici">
            <input className="input" value={form.vendor ?? ''} onChange={(e) => setForm({ ...form, vendor: e.target.value })} />
          </Field>

          <Field label="Lisans Tipi">
            <input
              className="input"
              value={form.licenseType ?? ''}
              onChange={(e) => setForm({ ...form, licenseType: e.target.value })}
            />
          </Field>

          <Field label="Lisans Adedi">
            <input
              type="number"
              className="input"
              value={form.licenseCount ?? ''}
              onChange={(e) => setForm({ ...form, licenseCount: e.target.value ? Number(e.target.value) : undefined })}
            />
          </Field>

          <Field label="Lisans Bitiş Tarihi">
            <input
              type="date"
              className="input"
              value={form.expiryDate ?? ''}
              onChange={(e) => setForm({ ...form, expiryDate: e.target.value })}
            />
          </Field>

          <Field label="Kurulum Yolu">
            <input
              className="input"
              value={form.installPath ?? ''}
              onChange={(e) => setForm({ ...form, installPath: e.target.value })}
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
            onClick={() => navigate('/applications')}
            className="rounded border border-slate-300 px-4 py-2 text-sm text-slate-600"
          >
            Vazgeç
          </button>
        </div>
      </form>
    </div>
  )
}
