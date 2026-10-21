import axios from 'axios'

const TOKEN_KEY = 'itam.token'

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
})

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

apiClient.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 401 oturumun düştüğü anlamına gelir (token süresi doldu/geçersiz/kullanıcı pasif);
// kullanıcıyı otomatik login'e atmak, her ekranda ayrı ayrı bunu ele almaktan kurtarır.
// Login isteğinin kendi 401'i hariç: o "şifre yanlış" demek, sayfa yenilenirse mesaj kaybolur.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const isLoginRequest = error.config?.url === '/auth/login'
    if (error.response?.status === 401 && !isLoginRequest) {
      clearToken()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

/** Backend'in RFC 7807 ProblemDetail formatındaki hata gövdesi. */
export interface ApiErrorDetail {
  row?: number
  field?: string
  code?: string
  message: string
  rejectedValue?: unknown
}

export interface ApiProblemDetail {
  title?: string
  detail?: string
  errors?: ApiErrorDetail[]
}

export function extractErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const problem = error.response?.data as ApiProblemDetail | undefined
    if (problem?.errors?.length) {
      return problem.errors.map((e) => e.message).join(', ')
    }
    if (problem?.detail) {
      return problem.detail
    }
  }
  return 'Beklenmeyen bir hata oluştu.'
}
