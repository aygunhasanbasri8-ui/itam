import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { createUser, getUser, updateUser } from '../../api/users'
import { listActivePeople } from '../../api/people'
import { extractErrorMessage } from '../../api/client'
import { Field } from '../../components/Field'
import { PersonSelect } from '../../components/PersonSelect'
import { USER_ROLE_LABELS, type UserRole } from '../../types/user'

interface UserForm {
  username: string
  password: string
  role: UserRole
  personId: number | null
  active: boolean
}

const EMPTY_FORM: UserForm = { username: '', password: '', role: 'USER', personId: null, active: true }

export function UserFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState<UserForm>(EMPTY_FORM)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const { data: people } = useQuery({ queryKey: ['people', 'active'], queryFn: listActivePeople })

  const { data: existing, isLoading } = useQuery({
    queryKey: ['users', id],
    queryFn: () => getUser(Number(id)),
    enabled: isEdit,
  })

  useEffect(() => {
    if (existing) {
      setForm({
        username: existing.username,
        password: '',
        role: existing.role,
        personId: existing.person?.id ?? null,
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
        await updateUser(Number(id), {
          password: form.password || null,
          role: form.role,
          personId: form.personId,
          active: form.active,
        })
      } else {
        await createUser({
          username: form.username,
          password: form.password,
          role: form.role,
          personId: form.personId,
        })
      }
      navigate('/users')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-xl space-y-4">
      <h1 className="text-xl font-semibold text-slate-800">{isEdit ? 'Kullanıcıyı Düzenle' : 'Yeni Kullanıcı'}</h1>

      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg border border-slate-200 bg-white p-6">
        <div className="grid grid-cols-2 gap-4">
          <Field label="Kullanıcı Adı" required>
            <input
              className="input"
              maxLength={80}
              value={form.username}
              disabled={isEdit}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              required
            />
          </Field>

          <Field label={isEdit ? 'Yeni Şifre (boşsa değişmez)' : 'Şifre'} required={!isEdit}>
            <input
              type="password"
              className="input"
              minLength={8}
              maxLength={72}
              autoComplete="new-password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              required={!isEdit}
            />
          </Field>

          <Field label="Rol" required>
            <select
              className="input"
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value as UserRole })}
            >
              {Object.entries(USER_ROLE_LABELS).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </Field>

          <Field label="Bağlı Kişi">
            <PersonSelect people={people} value={form.personId} onChange={(personId) => setForm({ ...form, personId })} />
          </Field>

          {isEdit && (
            <Field label="Aktif">
              <input
                type="checkbox"
                checked={form.active}
                onChange={(e) => setForm({ ...form, active: e.target.checked })}
              />
            </Field>
          )}
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
            onClick={() => navigate('/users')}
            className="rounded border border-slate-300 px-4 py-2 text-sm text-slate-600"
          >
            Vazgeç
          </button>
        </div>
      </form>
    </div>
  )
}
