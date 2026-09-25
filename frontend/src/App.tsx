import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { RequireAuth } from './auth/RequireAuth'
import { AppLayout } from './layout/AppLayout'
import { LoginPage } from './pages/LoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { ServerListPage } from './pages/servers/ServerListPage'
import { ServerFormPage } from './pages/servers/ServerFormPage'
import { WorkstationListPage } from './pages/workstations/WorkstationListPage'
import { WorkstationFormPage } from './pages/workstations/WorkstationFormPage'
import { CloudListPage } from './pages/clouds/CloudListPage'
import { CloudFormPage } from './pages/clouds/CloudFormPage'
import { ApplicationListPage } from './pages/applications/ApplicationListPage'
import { ApplicationFormPage } from './pages/applications/ApplicationFormPage'
import { BusinessServiceListPage } from './pages/business-services/BusinessServiceListPage'
import { BusinessServiceFormPage } from './pages/business-services/BusinessServiceFormPage'

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
              <Route path="/workstations" element={<WorkstationListPage />} />
              <Route path="/workstations/new" element={<WorkstationFormPage />} />
              <Route path="/workstations/:id" element={<WorkstationFormPage />} />
              <Route path="/clouds" element={<CloudListPage />} />
              <Route path="/clouds/new" element={<CloudFormPage />} />
              <Route path="/clouds/:id" element={<CloudFormPage />} />
              <Route path="/applications" element={<ApplicationListPage />} />
              <Route path="/applications/new" element={<ApplicationFormPage />} />
              <Route path="/applications/:id" element={<ApplicationFormPage />} />
              <Route path="/business-services" element={<BusinessServiceListPage />} />
              <Route path="/business-services/new" element={<BusinessServiceFormPage />} />
              <Route path="/business-services/:id" element={<BusinessServiceFormPage />} />
            </Route>
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
