import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import { login as loginRequest } from '../api/auth'
import { clearToken, getToken, setToken } from '../api/client'

interface AuthState {
  username: string | null
  role: string | null
}

interface AuthContextValue extends AuthState {
  isAuthenticated: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => void
}

const USERNAME_KEY = 'itam.username'
const ROLE_KEY = 'itam.role'

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({
    username: localStorage.getItem(USERNAME_KEY),
    role: localStorage.getItem(ROLE_KEY),
  })

  const value = useMemo<AuthContextValue>(
    () => ({
      ...state,
      isAuthenticated: Boolean(getToken()),
      login: async (username, password) => {
        const response = await loginRequest({ username, password })
        setToken(response.token)
        localStorage.setItem(USERNAME_KEY, response.username)
        localStorage.setItem(ROLE_KEY, response.role)
        setState({ username: response.username, role: response.role })
      },
      logout: () => {
        clearToken()
        localStorage.removeItem(USERNAME_KEY)
        localStorage.removeItem(ROLE_KEY)
        setState({ username: null, role: null })
      },
    }),
    [state],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth, AuthProvider içinde kullanılmalı')
  }
  return context
}
