import { useState } from 'react'
import { deleteAsset } from '../api/assets'
import { extractErrorMessage } from '../api/client'

export function DeleteAssetButton({ assetId, onDeleted }: { assetId: number; onDeleted: () => void }) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleClick() {
    if (!window.confirm('Bu varlık silinsin mi? Kayıt listelerden kalkar, değişiklik geçmişi saklanır.')) {
      return
    }
    setBusy(true)
    setError(null)
    try {
      await deleteAsset(assetId)
      onDeleted()
    } catch (err) {
      setError(extractErrorMessage(err))
      setBusy(false)
    }
  }

  return (
    <span className="ml-auto flex items-center gap-2">
      {error && <span className="text-sm text-red-600">{error}</span>}
      <button
        type="button"
        onClick={handleClick}
        disabled={busy}
        className="rounded border border-red-300 px-4 py-2 text-sm text-red-700 hover:bg-red-50 disabled:opacity-50"
      >
        Sil
      </button>
    </span>
  )
}
