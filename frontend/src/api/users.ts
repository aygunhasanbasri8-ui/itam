import { apiClient } from './client'
import type { AppUserRequest, AppUserResponse, AppUserUpdateRequest } from '../types/user'

export async function listUsers(): Promise<AppUserResponse[]> {
  const response = await apiClient.get<AppUserResponse[]>('/users')
  return response.data
}

export async function getUser(id: number): Promise<AppUserResponse> {
  const response = await apiClient.get<AppUserResponse>(`/users/${id}`)
  return response.data
}

export async function createUser(request: AppUserRequest): Promise<AppUserResponse> {
  const response = await apiClient.post<AppUserResponse>('/users', request)
  return response.data
}

export async function updateUser(id: number, request: AppUserUpdateRequest): Promise<AppUserResponse> {
  const response = await apiClient.put<AppUserResponse>(`/users/${id}`, request)
  return response.data
}
