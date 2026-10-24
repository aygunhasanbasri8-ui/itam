import { apiClient } from './client'
import type { AssetStatus } from '../types/common'

export async function deleteAsset(id: number): Promise<void> {
  await apiClient.delete(`/assets/${id}`)
}

export async function bulkUpdateStatus(assetIds: number[], status: AssetStatus): Promise<number> {
  const response = await apiClient.patch<{ updatedCount: number }>('/assets/bulk-status', { assetIds, status })
  return response.data.updatedCount
}

/** Listelerdeki "Excel'e aktar" için; dosya adı backend'in Content-Disposition başlığından gelir. */
export async function downloadExport(path: string, params: Record<string, string | undefined>): Promise<void> {
  const response = await apiClient.get<Blob>(path, { params, responseType: 'blob' })
  const disposition = String(response.headers['content-disposition'] ?? '')
  const filename = /filename="?([^";]+)"?/.exec(disposition)?.[1] ?? 'export.xlsx'

  const url = URL.createObjectURL(response.data)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

export interface ExportColumn {
  code: string
  header: string
}

export async function listExportColumns(exportPath: string): Promise<ExportColumn[]> {
  const response = await apiClient.get<ExportColumn[]>(`${exportPath}/columns`)
  return response.data
}

export interface ImportRowError {
  row: number
  column: string | null
  message: string
}

export interface ImportReport {
  dryRun: boolean
  totalRows: number
  validRows: number
  created: number
  errors: ImportRowError[]
}

export async function uploadImport(path: string, file: File, dryRun: boolean): Promise<ImportReport> {
  const form = new FormData()
  form.append('file', file)
  const response = await apiClient.post<ImportReport>(path, form, { params: { dryRun } })
  return response.data
}
