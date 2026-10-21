import type { PageResponse } from '../types/common'

export function Pagination({
  data,
  page,
  onPageChange,
}: {
  data: PageResponse<unknown>
  page: number
  onPageChange: (page: number) => void
}) {
  return (
    <div className="flex items-center justify-between text-sm text-slate-500">
      <span>
        Sayfa {data.page + 1} / {Math.max(data.totalPages, 1)} — Toplam {data.totalElements} kayıt
      </span>
      <div className="flex gap-2">
        <button
          type="button"
          disabled={page === 0}
          onClick={() => onPageChange(page - 1)}
          className="rounded border border-slate-300 px-3 py-1 disabled:opacity-40"
        >
          Önceki
        </button>
        <button
          type="button"
          disabled={data.last}
          onClick={() => onPageChange(page + 1)}
          className="rounded border border-slate-300 px-3 py-1 disabled:opacity-40"
        >
          Sonraki
        </button>
      </div>
    </div>
  )
}
