import { useState, type FormEvent, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { extractErrorMessage } from '../api/client'
import { useIsAdmin } from '../auth/useIsAdmin'
import type { AssetTypeCode } from '../types/assetType'
import { AuditTrailPanel } from './AuditTrailPanel'
import { CustomFieldInputs } from './CustomFieldInputs'
import { DeleteAssetButton } from './DeleteAssetButton'
import { DependencyPanel } from './DependencyPanel'

interface Props {
  title: string
  assetTypeCode: AssetTypeCode
  listPath: string
  // Düzenlemede kaydın id'si, yeni kayıtta null
  assetId: number | null
  customFields: Record<string, string> | undefined
  onCustomFieldsChange: (customFields: Record<string, string>) => void
  onSave: () => Promise<unknown>
  children: ReactNode
}

/**
 * Beş asset formunun ortak iskeleti: kaydet/vazgeç/sil, hata mesajı, özel alanlar,
 * USER rolünde salt okunur görünüm ve düzenlemede bağımlılık/değişiklik geçmişi panelleri.
 * Sayfalar yalnızca tipe özel alanları ve kaydetme çağrısını veriyor.
 */
export function AssetFormShell({
  title,
  assetTypeCode,
  listPath,
  assetId,
  customFields,
  onCustomFieldsChange,
  onSave,
  children,
}: Props) {
  const navigate = useNavigate()
  const isAdmin = useIsAdmin()
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await onSave()
      navigate(listPath)
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-2xl space-y-4">
      <h1 className="text-xl font-semibold text-slate-800">{title}</h1>

      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg border border-slate-200 bg-white p-6">
        <fieldset disabled={!isAdmin} className="space-y-4">
          {children}

          <CustomFieldInputs assetTypeCode={assetTypeCode} values={customFields} onChange={onCustomFieldsChange} />
        </fieldset>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <div className="flex gap-3">
          <button
            type="submit"
            hidden={!isAdmin}
            disabled={submitting}
            className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
          >
            {submitting ? 'Kaydediliyor...' : 'Kaydet'}
          </button>
          <button
            type="button"
            onClick={() => navigate(listPath)}
            className="rounded border border-slate-300 px-4 py-2 text-sm text-slate-600"
          >
            Vazgeç
          </button>
          {assetId !== null && isAdmin && (
            <DeleteAssetButton assetId={assetId} onDeleted={() => navigate(listPath)} />
          )}
        </div>
      </form>

      {assetId !== null && (
        <>
          <DependencyPanel assetId={assetId} />
          <AuditTrailPanel assetId={assetId} />
        </>
      )}
    </div>
  )
}
