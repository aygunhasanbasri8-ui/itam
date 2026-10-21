export interface DepartmentResponse {
  id: number
  name: string
  code: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface DepartmentRequest {
  name: string
  code: string
  active: boolean
}
