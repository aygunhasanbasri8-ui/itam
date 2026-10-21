import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { useIsAdmin } from '../../auth/useIsAdmin'
import { listPeople } from '../../api/people'
import { listActiveDepartments } from '../../api/departments'
import { Pagination } from '../../components/Pagination'

export function PersonListPage() {
  const isAdmin = useIsAdmin()
  const [search, setSearch] = useState('')
  const [departmentId, setDepartmentId] = useState('')
  const [onlyActive, setOnlyActive] = useState(true)
  const [page, setPage] = useState(0)

  const { data: departments } = useQuery({ queryKey: ['departments', 'active'], queryFn: listActiveDepartments })

  const { data, isLoading, isError } = useQuery({
    queryKey: ['people', { search, departmentId, onlyActive, page }],
    queryFn: () =>
      listPeople({
        search: search || undefined,
        departmentId: departmentId ? Number(departmentId) : undefined,
        active: onlyActive ? true : undefined,
        page,
        size: 20,
      }),
  })

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Kişiler</h1>
        {isAdmin && (
          <Link to="/people/new" className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white">
            Yeni Kişi
          </Link>
        )}
      </div>

      <div className="flex items-center gap-3">
        <input
          placeholder="Ad, sicil veya e-posta ara..."
          className="rounded border border-slate-300 px-3 py-2 text-sm"
          value={search}
          onChange={(e) => {
            setPage(0)
            setSearch(e.target.value)
          }}
        />
        <select
          className="rounded border border-slate-300 px-3 py-2 text-sm"
          value={departmentId}
          onChange={(e) => {
            setPage(0)
            setDepartmentId(e.target.value)
          }}
        >
          <option value="">Tüm departmanlar</option>
          {departments?.map((department) => (
            <option key={department.id} value={department.id}>
              {department.name}
            </option>
          ))}
        </select>
        <label className="flex items-center gap-2 text-sm text-slate-600">
          <input
            type="checkbox"
            checked={onlyActive}
            onChange={(e) => {
              setPage(0)
              setOnlyActive(e.target.checked)
            }}
          />
          Sadece aktifler
        </label>
      </div>

      {isLoading && <p className="text-slate-500">Yükleniyor...</p>}
      {isError && <p className="text-red-600">Kişiler alınamadı.</p>}

      {data && (
        <>
          <table className="w-full overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
            <thead className="bg-slate-100 text-left text-slate-600">
              <tr>
                <th className="px-4 py-2">Ad Soyad</th>
                <th className="px-4 py-2">Sicil</th>
                <th className="px-4 py-2">Unvan</th>
                <th className="px-4 py-2">Departman</th>
                <th className="px-4 py-2">E-posta</th>
                <th className="px-4 py-2">Durum</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((person) => (
                <tr key={person.id} className="border-t border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-2">
                    {person.system ? (
                      <span className="font-medium text-slate-800">{person.fullName}</span>
                    ) : (
                      <Link to={`/people/${person.id}`} className="font-medium text-slate-800 hover:underline">
                        {person.fullName}
                      </Link>
                    )}
                  </td>
                  <td className="px-4 py-2 text-slate-600">{person.code ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{person.title ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{person.department?.name ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">{person.email ?? '-'}</td>
                  <td className="px-4 py-2 text-slate-600">
                    {person.system ? 'Sistem kaydı' : person.active ? 'Aktif' : 'Pasif'}
                  </td>
                </tr>
              ))}
              {data.content.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-4 py-6 text-center text-slate-400">
                    Kayıt bulunamadı.
                  </td>
                </tr>
              )}
            </tbody>
          </table>

          <Pagination data={data} page={page} onPageChange={setPage} />
        </>
      )}
    </div>
  )
}
