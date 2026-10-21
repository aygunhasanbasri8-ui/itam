export interface FieldChange {
  field: string
  label: string
  oldValue: string | null
  newValue: string | null
}

export interface AssetRevision {
  revision: number
  timestamp: string
  username: string | null
  revisionType: 'ADD' | 'MOD' | 'DEL'
  changes: FieldChange[]
}
