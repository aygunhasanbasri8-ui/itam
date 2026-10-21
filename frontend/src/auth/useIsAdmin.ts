import { useAuth } from './AuthContext'

// Yazma uçları backend'de zaten ADMIN'e kapalı; bu sadece arayüzde işe yaramayacak butonları saklamak için
export function useIsAdmin(): boolean {
  return useAuth().role === 'ADMIN'
}
