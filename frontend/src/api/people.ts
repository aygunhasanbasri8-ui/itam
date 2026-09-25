import { apiClient } from './client'
import type { PageResponse, PersonSummary } from '../types/common'

/** Sorumlu kişi seçim kutuları için; en fazla 200 aktif kişi getirir (dropdown amaçlı, sayfalama yok). */
export async function listActivePeople(): Promise<PersonSummary[]> {
  const response = await apiClient.get<PageResponse<PersonSummary>>('/people', {
    params: { active: true, size: 200 },
  })
  return response.data.content
}
