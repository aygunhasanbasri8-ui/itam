import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { createCloud, getCloud, updateCloud } from '../../api/clouds'
import { listActivePeople } from '../../api/people'
import { AssetFormShell } from '../../components/AssetFormShell'
import { Field } from '../../components/Field'
import type { CloudAssetRequest, CloudProvider } from '../../types/cloud'

const EMPTY_FORM: CloudAssetRequest = { assetName: '', cloudProvider: 'AWS' }

export function CloudFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)

  const [form, setForm] = useState<CloudAssetRequest>(EMPTY_FORM)

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

  return (
    <AssetFormShell
      title={isEdit ? 'Cloud Varlığını Düzenle' : 'Yeni Cloud Varlığı'}
      assetTypeCode="CLOUD"
      listPath="/clouds"
      assetId={isEdit ? Number(id) : null}
      customFields={form.customFields}
      onCustomFieldsChange={(customFields) => setForm({ ...form, customFields })}
      onSave={() => (isEdit ? updateCloud(Number(id), form) : createCloud(form))}
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
    </AssetFormShell>
  )
}
