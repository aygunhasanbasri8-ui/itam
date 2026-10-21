import type { PersonSummary } from '../types/common'

interface Props {
  people: PersonSummary[] | undefined
  value: number | null | undefined
  onChange: (id: number | null) => void
  emptyLabel?: string
}

export function PersonSelect({ people, value, onChange, emptyLabel = '—' }: Props) {
  return (
    <select
      className="input"
      value={value ?? ''}
      onChange={(e) => onChange(e.target.value ? Number(e.target.value) : null)}
    >
      <option value="">{emptyLabel}</option>
      {people?.map((person) => (
        <option key={person.id} value={person.id}>
          {person.fullName}
        </option>
      ))}
    </select>
  )
}
