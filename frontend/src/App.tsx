import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { RequireAuth } from './auth/RequireAuth'
import { AppLayout } from './layout/AppLayout'
import { LoginPage } from './pages/LoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { ComingSoonPage } from './pages/ComingSoonPage'
import { ServerListPage } from './pages/servers/ServerListPage'
import { ServerFormPage } from './pages/servers/ServerFormPage'

const queryClient = new QueryClient()

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />

            <Route
              element={
                <RequireAuth>
                  <AppLayout />
                </RequireAuth>
              }
            >
              <Route path="/" element={<DashboardPage />} />
              <Route path="/servers" element={<ServerListPage />} />
              <Route path="/servers/new" element={<ServerFormPage />} />
              <Route path="/servers/:id" element={<ServerFormPage />} />
              <Route path="/workstations" element={<ComingSoonPage title="İş İstasyonları" />} />
              <Route path="/clouds" element={<ComingSoonPage title="Cloud Varlıkları" />} />
              <Route path="/applications" element={<ComingSoonPage title="Uygulama & Yazılım" />} />
              <Route path="/business-services" element={<ComingSoonPage title="İş Servisleri" />} />
            </Route>
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
