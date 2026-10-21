import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { createDependency, deleteDependency, getDependencyTree, listAssetDependencies } from '../api/dependencies'
import { searchAssets } from '../api/assetSearch'
import { useDebouncedValue } from '../hooks/useDebouncedValue'
import { extractErrorMessage } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { ASSET_TYPE_LABELS } from '../types/assetType'
import type { DependencyTreeNode, DependencyType } from '../types/dependency'

const DEPENDENCY_TYPE_LABELS: Record<DependencyType, string> = {
  RUNS_ON: 'Üzerinde çalışır',
  USES: 'Kullanır',
  DEPENDS_ON: 'Bağımlıdır',
}

type Direction = 'OUTGOING' | 'INCOMING'

export function DependencyPanel({ assetId }: { assetId: number }) {
  const queryClient = useQueryClient()
  const { role } = useAuth()
  const canEdit = role === 'ADMIN'

  const { data: edges } = useQuery({
    queryKey: ['dependencies', assetId],
    queryFn: () => listAssetDependencies(assetId),
  })
  const { data: tree } = useQuery({
    queryKey: ['dependency-tree', assetId],
    queryFn: () => getDependencyTree(assetId),
  })

  // Bağımlılık değişince hem kenar listesi hem ağaç bayatlar; diğer asset'lerin
  // ağaçları da etkilenebildiği için anahtarın tamamını geçersiz kılıyoruz.
  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['dependencies'] })
    queryClient.invalidateQueries({ queryKey: ['dependency-tree'] })
  }

  const [removeError, setRemoveError] = useState<string | null>(null)
  const removeMutation = useMutation({
    mutationFn: deleteDependency,
    onSuccess: () => {
      setRemoveError(null)
      invalidate()
    },
    onError: (err) => setRemoveError(extractErrorMessage(err)),
  })

  const outgoing = edges?.filter((edge) => edge.parentAssetId === assetId) ?? []
  const incoming = edges?.filter((edge) => edge.childAssetId === assetId) ?? []

  return (
    <section className="space-y-4 rounded-lg border border-slate-200 bg-white p-6">
      <h2 className="text-lg font-semibold text-slate-800">Bağımlılıklar</h2>

      <div className="grid grid-cols-2 gap-6 text-sm">
        <EdgeList
          title="Bağımlı olduğu varlıklar"
          empty="Bu varlık hiçbir varlığa bağımlı değil."
          items={outgoing.map((edge) => ({
            id: edge.id,
            name: edge.childAssetName,
            type: edge.dependencyType,
            notes: edge.notes,
          }))}
          onRemove={canEdit ? (id) => removeMutation.mutate(id) : undefined}
        />
        <EdgeList
          title="Bu varlığa bağımlı olanlar"
          empty="Bu varlığa bağımlı bir varlık yok."
          items={incoming.map((edge) => ({
            id: edge.id,
            name: edge.parentAssetName,
            type: edge.dependencyType,
            notes: edge.notes,
          }))}
          onRemove={canEdit ? (id) => removeMutation.mutate(id) : undefined}
        />
      </div>
      {removeError && <p className="text-sm text-red-600">{removeError}</p>}

      {canEdit && <AddDependencyForm assetId={assetId} onAdded={invalidate} />}

      {tree && tree.children.length > 0 && (
        <div className="text-sm">
          <h3 className="mb-2 font-medium text-slate-600">Bağımlılık ağacı</h3>
          <TreeNode node={tree} />
        </div>
      )}
    </section>
  )
}

interface EdgeItem {
  id: number
  name: string
  type: DependencyType
  notes: string | null
}

function EdgeList({
  title,
  empty,
  items,
  onRemove,
}: {
  title: string
  empty: string
  items: EdgeItem[]
  onRemove?: (id: number) => void
}) {
  return (
    <div>
      <h3 className="mb-2 font-medium text-slate-600">{title}</h3>
      {items.length === 0 ? (
        <p className="text-slate-400">{empty}</p>
      ) : (
        <ul className="divide-y divide-slate-100 rounded border border-slate-200">
          {items.map((item) => (
            <li key={item.id} className="flex items-center justify-between gap-2 px-3 py-2">
              <div>
                <span className="font-medium text-slate-700">{item.name}</span>
                <span className="ml-2 text-xs text-slate-500">{DEPENDENCY_TYPE_LABELS[item.type]}</span>
                {item.notes && <p className="text-xs text-slate-400">{item.notes}</p>}
              </div>
              {onRemove && (
                <button
                  type="button"
                  onClick={() => onRemove(item.id)}
                  className="text-xs text-red-600 hover:underline"
                >
                  Kaldır
                </button>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

function AddDependencyForm({ assetId, onAdded }: { assetId: number; onAdded: () => void }) {
  const [search, setSearch] = useState('')
  const [targetId, setTargetId] = useState('')
  const [direction, setDirection] = useState<Direction>('OUTGOING')
  const [dependencyType, setDependencyType] = useState<DependencyType>('DEPENDS_ON')
  const [notes, setNotes] = useState('')
  const [error, setError] = useState<string | null>(null)

  // Her tuşta istek atmasın diye yazma bitince aranıyor
  const term = useDebouncedValue(search.trim())
  const { data: results } = useQuery({
    queryKey: ['asset-search', term],
    queryFn: () => searchAssets(term),
  })
  const candidates = (results ?? []).filter((option) => option.id !== assetId)

  const mutation = useMutation({
    mutationFn: createDependency,
    onSuccess: () => {
      setTargetId('')
      setNotes('')
      setError(null)
      onAdded()
    },
    onError: (err) => setError(extractErrorMessage(err)),
  })

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!targetId) {
      setError('Hedef varlık seçilmedi.')
      return
    }
    const target = Number(targetId)
    mutation.mutate({
      parentAssetId: direction === 'OUTGOING' ? assetId : target,
      childAssetId: direction === 'OUTGOING' ? target : assetId,
      dependencyType,
      notes: notes || null,
    })
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-3 rounded border border-dashed border-slate-300 p-4 text-sm">
      <h3 className="font-medium text-slate-600">Bağımlılık ekle</h3>
      <div className="grid grid-cols-2 gap-3">
        <select className="input" value={direction} onChange={(e) => setDirection(e.target.value as Direction)}>
          <option value="OUTGOING">Bu varlık, seçilen varlığa...</option>
          <option value="INCOMING">Seçilen varlık, bu varlığa...</option>
        </select>
        <select
          className="input"
          value={dependencyType}
          onChange={(e) => setDependencyType(e.target.value as DependencyType)}
        >
          {Object.entries(DEPENDENCY_TYPE_LABELS).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>
        <input
          className="input"
          placeholder="Varlık adıyla ara..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <select className="input" value={targetId} onChange={(e) => setTargetId(e.target.value)}>
          <option value="">Varlık seçin ({candidates.length})</option>
          {candidates.map((option) => (
            <option key={option.id} value={option.id}>
              {option.assetName} — {ASSET_TYPE_LABELS[option.assetTypeCode]}
            </option>
          ))}
        </select>
      </div>
      <input
        className="input"
        placeholder="Not (opsiyonel)"
        maxLength={255}
        value={notes}
        onChange={(e) => setNotes(e.target.value)}
      />
      {error && <p className="text-red-600">{error}</p>}
      <button
        type="submit"
        disabled={mutation.isPending}
        className="rounded bg-slate-900 px-4 py-2 font-medium text-white disabled:opacity-50"
      >
        Ekle
      </button>
    </form>
  )
}

function TreeNode({ node }: { node: DependencyTreeNode }) {
  return (
    <div>
      <span className="text-slate-700">{node.assetName}</span>
      {node.dependencyType && (
        <span className="ml-2 text-xs text-slate-400">({DEPENDENCY_TYPE_LABELS[node.dependencyType]})</span>
      )}
      {node.children.length > 0 && (
        <div className="ml-4 border-l border-slate-200 pl-3">
          {node.children.map((child) => (
            <TreeNode key={`${node.assetId}-${child.assetId}`} node={child} />
          ))}
        </div>
      )}
    </div>
  )
}
