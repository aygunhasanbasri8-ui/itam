import { useQuery } from '@tanstack/react-query'
import { listActivePeople } from '../api/people'

/** Liste ekranlarında "sorumluya göre" filtresi; değer boş metinse filtre yok. */
export function OwnerFilter({ value, onChange }: { value: string; onChange: (value: string) => void }) {
  const { data: people } = useQuery({ queryKey: ['people', 'active'], queryFn: listActivePeople })

  return (
    <select
      className="rounded border border-slate-300 px-3 py-2 text-sm"
      value={value}
      onChange={(e) => onChange(e.target.value)}
    >
      <option value="">Tüm sorumlular</option>
      {people?.map((person) => (
        <option key={person.id} value={person.id}>
          {person.fullName}
        </option>
      ))}
    </select>
  )
}
