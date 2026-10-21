import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { createServer, getServer, updateServer } from '../../api/servers'
import { listActivePeople } from '../../api/people'
import { extractErrorMessage } from '../../api/client'
import { Field } from '../../components/Field'
import { CustomFieldInputs } from '../../components/CustomFieldInputs'
import { DependencyPanel } from '../../components/DependencyPanel'
import { AuditTrailPanel } from '../../components/AuditTrailPanel'
import type { ServerAssetRequest, ServerEnvironment } from '../../types/server'

const EMPTY_FORM: ServerAssetRequest = {
  assetName: '',
  hostname: '',
  environment: 'PROD',
  virtual: false,
  notes: '',
  domain: '',
  ipAddress: '',
  os: '',
}

export function ServerFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState<ServerAssetRequest>(EMPTY_FORM)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const { data: people } = useQuery({ queryKey: ['people', 'active'], queryFn: listActivePeople })

  const { data: existing, isLoading } = useQuery({
    queryKey: ['servers', id],
    queryFn: () => getServer(Number(id)),
    enabled: isEdit,
  })

  useEffect(() => {
    if (existing) {
      setForm({
        assetName: existing.assetName,
        hostname: existing.hostname,
        environment: existing.environment,
        virtual: existing.virtual,
        notes: existing.notes ?? '',
        domain: existing.domain ?? '',
        ipAddress: existing.ipAddress ?? '',
        os: existing.os ?? '',
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
        await updateServer(Number(id), form)
      } else {
        await createServer(form)
      }
      navigate('/servers')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-2xl space-y-4">
      <h1 className="text-xl font-semibold text-slate-800">{isEdit ? 'Sunucuyu Düzenle' : 'Yeni Sunucu'}</h1>

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

          <Field label="Hostname" required>
            <input
              className="input"
              value={form.hostname}
              onChange={(e) => setForm({ ...form, hostname: e.target.value })}
              required
            />
          </Field>

          <Field label="Ortam" required>
            <select
              className="input"
              value={form.environment}
              onChange={(e) => setForm({ ...form, environment: e.target.value as ServerEnvironment })}
            >
              <option value="DEV">DEV</option>
              <option value="TEST">TEST</option>
              <option value="UAT">UAT</option>
              <option value="PROD">PROD</option>
            </select>
          </Field>

          <Field label="IP Adresi">
            <input
              className="input"
              value={form.ipAddress ?? ''}
              onChange={(e) => setForm({ ...form, ipAddress: e.target.value })}
            />
          </Field>

          <Field label="Domain">
            <input
              className="input"
              value={form.domain ?? ''}
              onChange={(e) => setForm({ ...form, domain: e.target.value })}
            />
          </Field>

          <Field label="İşletim Sistemi">
            <input className="input" value={form.os ?? ''} onChange={(e) => setForm({ ...form, os: e.target.value })} />
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
              {people?.map((person) => (
                <option key={person.id} value={person.id}>
                  {person.fullName}
                </option>
              ))}
            </select>
          </Field>

          <Field label="Sanal mı?">
            <input
              type="checkbox"
              checked={form.virtual}
              onChange={(e) => setForm({ ...form, virtual: e.target.checked })}
            />
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

        <CustomFieldInputs
          assetTypeCode="SERVER"
          values={form.customFields}
          onChange={(customFields) => setForm({ ...form, customFields })}
        />

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
            onClick={() => navigate('/servers')}
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
