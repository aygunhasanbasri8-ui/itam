import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { createApplication, getApplication, updateApplication } from '../../api/applications'
import { listActivePeople } from '../../api/people'
import { AssetFormShell } from '../../components/AssetFormShell'
import { Field } from '../../components/Field'
import type { ApplicationAssetRequest } from '../../types/application'

const EMPTY_FORM: ApplicationAssetRequest = { assetName: '' }

export function ApplicationFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)

  const [form, setForm] = useState<ApplicationAssetRequest>(EMPTY_FORM)

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

  return (
    <AssetFormShell
      title={isEdit ? 'Uygulamayı Düzenle' : 'Yeni Uygulama'}
      assetTypeCode="APPLICATION"
      listPath="/applications"
      assetId={isEdit ? Number(id) : null}
      customFields={form.customFields}
      onCustomFieldsChange={(customFields) => setForm({ ...form, customFields })}
      onSave={() => (isEdit ? updateApplication(Number(id), form) : createApplication(form))}
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
    </AssetFormShell>
  )
}
