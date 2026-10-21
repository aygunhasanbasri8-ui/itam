import { useState } from 'react'

export function useSelection() {
  const [selected, setSelected] = useState<Set<number>>(new Set())

  function toggle(id: number) {
    setSelected((prev) => {
      const next = new Set(prev)
      if (next.has(id)) {
        next.delete(id)
      } else {
        next.add(id)
      }
      return next
    })
  }

  // Sayfadakilerin hepsi seçiliyse hepsini bırakır, değilse hepsini ekler
  function toggleAll(ids: number[]) {
    setSelected((prev) => {
      const next = new Set(prev)
      const allSelected = ids.length > 0 && ids.every((id) => next.has(id))
      ids.forEach((id) => (allSelected ? next.delete(id) : next.add(id)))
      return next
    })
  }

  function isAllSelected(ids: number[]) {
    return ids.length > 0 && ids.every((id) => selected.has(id))
  }

  return {
    ids: [...selected],
    has: (id: number) => selected.has(id),
    toggle,
    toggleAll,
    isAllSelected,
    clear: () => setSelected(new Set()),
  }
}

export type Selection = ReturnType<typeof useSelection>
