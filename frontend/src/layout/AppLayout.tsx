import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/servers', label: 'Sunucular' },
  { to: '/workstations', label: 'İş İstasyonları' },
  { to: '/clouds', label: 'Cloud Varlıkları' },
  { to: '/applications', label: 'Uygulama & Yazılım' },
  { to: '/business-services', label: 'İş Servisleri' },
  { to: '/people', label: 'Kişiler' },
  { to: '/departments', label: 'Departmanlar' },
  { to: '/custom-fields', label: 'Özel Alanlar' },
  { to: '/users', label: 'Kullanıcılar', adminOnly: true },
]

export function AppLayout() {
  const { username, role, logout } = useAuth()

  return (
    <div className="flex h-screen bg-slate-50">
      <aside className="w-60 shrink-0 border-r border-slate-200 bg-white">
        <div className="px-4 py-4 text-lg font-semibold text-slate-800">ITAM</div>
        <nav className="flex flex-col gap-1 px-2">
          {NAV_ITEMS.filter((item) => !item.adminOnly || role === 'ADMIN').map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                `rounded px-3 py-2 text-sm font-medium ${
                  isActive ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-3">
          <span className="text-sm text-slate-500">Kurumsal IT Asset Management</span>
          <div className="flex items-center gap-3 text-sm">
            <span className="text-slate-700">{username}</span>
            <button
              type="button"
              onClick={logout}
              className="rounded border border-slate-300 px-3 py-1 text-slate-600 hover:bg-slate-100"
            >
              Çıkış
            </button>
          </div>
        </header>

        <main className="flex-1 overflow-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
