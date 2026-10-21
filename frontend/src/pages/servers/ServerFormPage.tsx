import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { createServer, getServer, updateServer } from '../../api/servers'
import { listActivePeople } from '../../api/people'
import { AssetFormShell } from '../../components/AssetFormShell'
import { Field } from '../../components/Field'
import { PersonSelect } from '../../components/PersonSelect'
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

  const [form, setForm] = useState<ServerAssetRequest>(EMPTY_FORM)

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
        serverClass: existing.serverClass ?? '',
        infrastructureType: existing.infrastructureType ?? '',
        groupName: existing.groupName ?? '',
        groupInfo: existing.groupInfo ?? '',
        groupDesc: existing.groupDesc ?? '',
        responsibilityOwnerId: existing.responsibilityOwner?.id ?? null,
        serviceOwnerId: existing.serviceOwner?.id ?? null,
        appResponsibleId: existing.appResponsible?.id ?? null,
        osResponsibleId: existing.osResponsible?.id ?? null,
        customFields: existing.customFields,
      })
    }
  }, [existing])

  if (isEdit && isLoading) {
    return <p className="text-slate-500">Yükleniyor...</p>
  }

  return (
    <AssetFormShell
      title={isEdit ? 'Sunucuyu Düzenle' : 'Yeni Sunucu'}
      assetTypeCode="SERVER"
      listPath="/servers"
      assetId={isEdit ? Number(id) : null}
      customFields={form.customFields}
      onCustomFieldsChange={(customFields) => setForm({ ...form, customFields })}
      onSave={() => (isEdit ? updateServer(Number(id), form) : createServer(form))}
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
          <PersonSelect
            people={people}
            value={form.responsibilityOwnerId}
            onChange={(responsibilityOwnerId) => setForm({ ...form, responsibilityOwnerId })}
            emptyLabel="FIRMA (varsayılan)"
          />
        </Field>

        <Field label="Sunucu Sınıfı">
          <input
            className="input"
            value={form.serverClass ?? ''}
            onChange={(e) => setForm({ ...form, serverClass: e.target.value })}
          />
        </Field>

        <Field label="Altyapı Tipi">
          <input
            className="input"
            value={form.infrastructureType ?? ''}
            onChange={(e) => setForm({ ...form, infrastructureType: e.target.value })}
          />
        </Field>

        <Field label="Grup Adı">
          <input
            className="input"
            value={form.groupName ?? ''}
            onChange={(e) => setForm({ ...form, groupName: e.target.value })}
          />
        </Field>

        <Field label="Grup Bilgisi">
          <input
            className="input"
            value={form.groupInfo ?? ''}
            onChange={(e) => setForm({ ...form, groupInfo: e.target.value })}
          />
        </Field>

        <Field label="Servis Sahibi">
          <PersonSelect
            people={people}
            value={form.serviceOwnerId}
            onChange={(serviceOwnerId) => setForm({ ...form, serviceOwnerId })}
          />
        </Field>

        <Field label="Uygulama Sorumlusu">
          <PersonSelect
            people={people}
            value={form.appResponsibleId}
            onChange={(appResponsibleId) => setForm({ ...form, appResponsibleId })}
          />
        </Field>

        <Field label="OS Sorumlusu">
          <PersonSelect
            people={people}
            value={form.osResponsibleId}
            onChange={(osResponsibleId) => setForm({ ...form, osResponsibleId })}
          />
        </Field>

        <Field label="Sanal mı?">
          <input
            type="checkbox"
            checked={form.virtual}
            onChange={(e) => setForm({ ...form, virtual: e.target.checked })}
          />
        </Field>
      </div>

      <Field label="Grup Açıklaması">
        <input
          className="input"
          value={form.groupDesc ?? ''}
          onChange={(e) => setForm({ ...form, groupDesc: e.target.value })}
        />
      </Field>

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
