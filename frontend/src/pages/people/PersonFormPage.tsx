import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { createPerson, getPerson, updatePerson } from '../../api/people'
import { listActiveDepartments } from '../../api/departments'
import { extractErrorMessage } from '../../api/client'
import { useIsAdmin } from '../../auth/useIsAdmin'
import { Field } from '../../components/Field'
import type { PersonRequest } from '../../types/person'

const EMPTY_FORM: PersonRequest = {
  fullName: '',
  code: '',
  email: '',
  title: '',
  phone: '',
  departmentId: null,
  active: true,
}

export function PersonFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const isAdmin = useIsAdmin()
  const navigate = useNavigate()

  const [form, setForm] = useState<PersonRequest>(EMPTY_FORM)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const { data: departments } = useQuery({ queryKey: ['departments', 'active'], queryFn: listActiveDepartments })

  const { data: existing, isLoading } = useQuery({
    queryKey: ['people', id],
    queryFn: () => getPerson(Number(id)),
    enabled: isEdit,
  })

  useEffect(() => {
    if (existing) {
      setForm({
        fullName: existing.fullName,
        code: existing.code ?? '',
        email: existing.email ?? '',
        title: existing.title ?? '',
        phone: existing.phone ?? '',
        departmentId: existing.department?.id ?? null,
        active: existing.active,
      })
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
        await updatePerson(Number(id), form)
      } else {
        await createPerson(form)
      }
      navigate('/people')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-2xl space-y-4">
      <h1 className="text-xl font-semibold text-slate-800">{isEdit ? 'Kişiyi Düzenle' : 'Yeni Kişi'}</h1>

      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg border border-slate-200 bg-white p-6">
        <fieldset disabled={!isAdmin} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <Field label="Ad Soyad" required>
              <input
                className="input"
                maxLength={150}
                value={form.fullName}
                onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                required
              />
            </Field>

            <Field label="Sicil No">
              <input
                className="input"
                maxLength={40}
                value={form.code ?? ''}
                onChange={(e) => setForm({ ...form, code: e.target.value })}
              />
            </Field>

            <Field label="E-posta">
              <input
                type="email"
                className="input"
                maxLength={180}
                value={form.email ?? ''}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
              />
            </Field>

            <Field label="Telefon">
              <input
                className="input"
                maxLength={30}
                value={form.phone ?? ''}
                onChange={(e) => setForm({ ...form, phone: e.target.value })}
              />
            </Field>

            <Field label="Unvan">
              <input
                className="input"
                maxLength={120}
                value={form.title ?? ''}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
              />
            </Field>

            <Field label="Departman">
              <select
                className="input"
                value={form.departmentId ?? ''}
                onChange={(e) => setForm({ ...form, departmentId: e.target.value ? Number(e.target.value) : null })}
              >
                <option value="">—</option>
                {departments?.map((department) => (
                  <option key={department.id} value={department.id}>
                    {department.name}
                  </option>
                ))}
              </select>
            </Field>

            <Field label="Aktif">
              <input
                type="checkbox"
                checked={form.active}
                onChange={(e) => setForm({ ...form, active: e.target.checked })}
              />
            </Field>
          </div>
        </fieldset>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <div className="flex gap-3">
          <button
            type="submit"
            hidden={!isAdmin}
            disabled={submitting}
            className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
          >
            {submitting ? 'Kaydediliyor...' : 'Kaydet'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/people')}
            className="rounded border border-slate-300 px-4 py-2 text-sm text-slate-600"
          >
            Vazgeç
          </button>
        </div>
      </form>
    </div>
  )
}
