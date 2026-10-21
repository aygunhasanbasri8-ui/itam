import { apiClient } from './client'
import type { PageResponse, PersonSummary } from '../types/common'
import type { PersonRequest, PersonResponse } from '../types/person'

/** Sorumlu kişi seçim kutuları için; en fazla 200 aktif kişi getirir (dropdown amaçlı, sayfalama yok). */
export async function listActivePeople(): Promise<PersonSummary[]> {
  const response = await apiClient.get<PageResponse<PersonSummary>>('/people', {
    params: { active: true, size: 200 },
  })
  return response.data.content
}

export interface PersonListParams {
  page?: number
  size?: number
  search?: string
  active?: boolean
  departmentId?: number
}

export async function listPeople(params: PersonListParams): Promise<PageResponse<PersonResponse>> {
  const response = await apiClient.get<PageResponse<PersonResponse>>('/people', { params })
  return response.data
}

export async function getPerson(id: number): Promise<PersonResponse> {
  const response = await apiClient.get<PersonResponse>(`/people/${id}`)
  return response.data
}

export async function createPerson(request: PersonRequest): Promise<PersonResponse> {
  const response = await apiClient.post<PersonResponse>('/people', request)
  return response.data
}

export async function updatePerson(id: number, request: PersonRequest): Promise<PersonResponse> {
  const response = await apiClient.put<PersonResponse>(`/people/${id}`, request)
  return response.data
}
