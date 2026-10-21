import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { listUsers } from '../../api/users'
import { USER_ROLE_LABELS } from '../../types/user'

export function UserListPage() {
  const { data, isLoading, isError } = useQuery({ queryKey: ['users'], queryFn: listUsers })

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Kullanıcılar</h1>
        <Link to="/users/new" className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white">
          Yeni Kullanıcı
        </Link>
      </div>

      {isLoading && <p className="text-slate-500">Yükleniyor...</p>}
      {isError && <p className="text-red-600">Kullanıcılar alınamadı.</p>}

      {data && (
        <table className="w-full overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
          <thead className="bg-slate-100 text-left text-slate-600">
            <tr>
              <th className="px-4 py-2">Kullanıcı Adı</th>
              <th className="px-4 py-2">Rol</th>
              <th className="px-4 py-2">Bağlı Kişi</th>
              <th className="px-4 py-2">Son Giriş</th>
              <th className="px-4 py-2">Durum</th>
            </tr>
          </thead>
          <tbody>
            {data.map((user) => (
              <tr key={user.id} className="border-t border-slate-100 hover:bg-slate-50">
                <td className="px-4 py-2">
                  <Link to={`/users/${user.id}`} className="font-medium text-slate-800 hover:underline">
                    {user.username}
                  </Link>
                </td>
                <td className="px-4 py-2 text-slate-600">{USER_ROLE_LABELS[user.role]}</td>
                <td className="px-4 py-2 text-slate-600">{user.person?.fullName ?? '-'}</td>
                <td className="px-4 py-2 text-slate-600">
                  {user.lastLoginAt ? new Date(user.lastLoginAt).toLocaleString('tr-TR') : '-'}
                </td>
                <td className="px-4 py-2 text-slate-600">{user.active ? 'Aktif' : 'Pasif'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
