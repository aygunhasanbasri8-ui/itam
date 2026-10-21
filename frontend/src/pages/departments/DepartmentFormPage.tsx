import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { createDepartment, getDepartment, updateDepartment } from '../../api/departments'
import { extractErrorMessage } from '../../api/client'
import { Field } from '../../components/Field'
import type { DepartmentRequest } from '../../types/department'

const EMPTY_FORM: DepartmentRequest = { name: '', code: '', active: true }

export function DepartmentFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState<DepartmentRequest>(EMPTY_FORM)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const { data: existing, isLoading } = useQuery({
    queryKey: ['departments', id],
    queryFn: () => getDepartment(Number(id)),
    enabled: isEdit,
  })

  useEffect(() => {
    if (existing) {
      setForm({ name: existing.name, code: existing.code, active: existing.active })
    }
  }, [existing])

  if (isEdit && isLoading) {
    return <p className="text-slate-500">Yükleniyor...</p>
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      if (isEdit) {
        await updateDepartment(Number(id), form)
      } else {
        await createDepartment(form)
      }
      navigate('/departments')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-xl space-y-4">
      <h1 className="text-xl font-semibold text-slate-800">{isEdit ? 'Departmanı Düzenle' : 'Yeni Departman'}</h1>

      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg border border-slate-200 bg-white p-6">
        <div className="grid grid-cols-2 gap-4">
          <Field label="Ad" required>
            <input
              className="input"
              maxLength={150}
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
            />
          </Field>

          <Field label="Kod" required>
            <input
              className="input"
              maxLength={30}
              value={form.code}
              onChange={(e) => setForm({ ...form, code: e.target.value })}
              required
            />
          </Field>

          <Field label="Aktif">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm({ ...form, active: e.target.checked })}
            />
          </Field>
        </div>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <div className="flex gap-3">
          <button
            type="submit"
            disabled={submitting}
            className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
          >
            {submitting ? 'Kaydediliyor...' : 'Kaydet'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/departments')}
            className="rounded border border-slate-300 px-4 py-2 text-sm text-slate-600"
          >
            Vazgeç
          </button>
        </div>
      </form>
    </div>
  )
}
