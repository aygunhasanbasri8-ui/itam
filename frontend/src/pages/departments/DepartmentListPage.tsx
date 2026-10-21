import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { listDepartments } from '../../api/departments'
import { Pagination } from '../../components/Pagination'

export function DepartmentListPage() {
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)

  const { data, isLoading, isError } = useQuery({
    queryKey: ['departments', { search, page }],
    queryFn: () => listDepartments({ search: search || undefined, page, size: 20 }),
  })

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-800">Departmanlar</h1>
        <Link to="/departments/new" className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white">
          Yeni Departman
        </Link>
      </div>

      <input
        placeholder="Ad veya kod ara..."
        className="input max-w-xs"
        value={search}
        onChange={(e) => {
          setPage(0)
          setSearch(e.target.value)
        }}
      />

      {isLoading && <p className="text-slate-500">Yükleniyor...</p>}
      {isError && <p className="text-red-600">Departmanlar alınamadı.</p>}

      {data && (
        <>
          <table className="w-full overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
            <thead className="bg-slate-100 text-left text-slate-600">
              <tr>
                <th className="px-4 py-2">Ad</th>
                <th className="px-4 py-2">Kod</th>
                <th className="px-4 py-2">Durum</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((department) => (
                <tr key={department.id} className="border-t border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-2">
                    <Link
                      to={`/departments/${department.id}`}
                      className="font-medium text-slate-800 hover:underline"
                    >
                      {department.name}
                    </Link>
                  </td>
                  <td className="px-4 py-2 text-slate-600">{department.code}</td>
                  <td className="px-4 py-2 text-slate-600">{department.active ? 'Aktif' : 'Pasif'}</td>
                </tr>
              ))}
              {data.content.length === 0 && (
                <tr>
                  <td colSpan={3} className="px-4 py-6 text-center text-slate-400">
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
