import type { PersonSummary } from './common'

export type UserRole = 'ADMIN' | 'USER'

export const USER_ROLE_LABELS: Record<UserRole, string> = {
  ADMIN: 'Yönetici',
  USER: 'Kullanıcı',
}

export interface AppUserResponse {
  id: number
  username: string
  role: UserRole
  person: PersonSummary | null
  active: boolean
  lastLoginAt: string | null
  createdAt: string
}

export interface AppUserRequest {
  username: string
  password: string
  role: UserRole
  personId?: number | null
}

export interface AppUserUpdateRequest {
  // Boş bırakılırsa şifre değişmez
  password?: string | null
  role: UserRole
  personId?: number | null
  active: boolean
}
