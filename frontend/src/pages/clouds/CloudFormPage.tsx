import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { createCloud, getCloud, updateCloud } from '../../api/clouds'
import { listActivePeople } from '../../api/people'
import { extractErrorMessage } from '../../api/client'
import { Field } from '../../components/Field'
import { CustomFieldInputs } from '../../components/CustomFieldInputs'
import { DependencyPanel } from '../../components/DependencyPanel'
import { AuditTrailPanel } from '../../components/AuditTrailPanel'
import type { CloudAssetRequest, CloudProvider } from '../../types/cloud'

const EMPTY_FORM: CloudAssetRequest = { assetName: '', cloudProvider: 'AWS' }

export function CloudFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState<CloudAssetRequest>(EMPTY_FORM)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const { data: people } = useQuery({ queryKey: ['people', 'active'], queryFn: listActivePeople })

  const { data: existing, isLoading } = useQuery({
    queryKey: ['clouds', id],
    queryFn: () => getCloud(Number(id)),
    enabled: isEdit,
  })

  useEffect(() => {
    if (existing) {
      setForm({
        assetName: existing.assetName,
        cloudProvider: existing.cloudProvider,
        notes: existing.notes ?? '',
        instanceType: existing.instanceType ?? '',
        region: existing.region ?? '',
        accountId: existing.accountId ?? '',
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
        await updateCloud(Number(id), form)
      } else {
        await createCloud(form)
      }
      navigate('/clouds')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-2xl space-y-4">
      <h1 className="text-xl font-semibold text-slate-800">{isEdit ? 'Cloud Varlığını Düzenle' : 'Yeni Cloud Varlığı'}</h1>

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

          <Field label="Sağlayıcı" required>
            <select
              className="input"
              value={form.cloudProvider}
              onChange={(e) => setForm({ ...form, cloudProvider: e.target.value as CloudProvider })}
            >
              <option value="AWS">AWS</option>
              <option value="AZURE">AZURE</option>
              <option value="GCP">GCP</option>
              <option value="OTHER">OTHER</option>
            </select>
          </Field>

          <Field label="Instance Tipi">
            <input
              className="input"
              value={form.instanceType ?? ''}
              onChange={(e) => setForm({ ...form, instanceType: e.target.value })}
            />
          </Field>

          <Field label="Bölge">
            <input className="input" value={form.region ?? ''} onChange={(e) => setForm({ ...form, region: e.target.value })} />
          </Field>

          <Field label="Hesap Kimliği">
            <input
              className="input"
              value={form.accountId ?? ''}
              onChange={(e) => setForm({ ...form, accountId: e.target.value })}
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

        <CustomFieldInputs
          assetTypeCode="CLOUD"
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
            onClick={() => navigate('/clouds')}
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
