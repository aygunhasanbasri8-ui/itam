import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ImportButton } from './ImportButton'

const uploadImport = vi.fn()
vi.mock('../api/assets', () => ({
  uploadImport: (...args: unknown[]) => uploadImport(...args),
  downloadExport: vi.fn(),
}))

function renderButton() {
  render(
    <QueryClientProvider client={new QueryClient()}>
      <ImportButton path="/servers/import" queryKey="servers" />
    </QueryClientProvider>,
  )
  fireEvent.click(screen.getByText("Excel'den içe aktar"))
  const input = document.querySelector('input[type="file"]') as HTMLInputElement
  fireEvent.change(input, { target: { files: [new File(['x'], 'sunucular.xlsx')] } })
}

afterEach(() => {
  cleanup()
  uploadImport.mockReset()
})

describe('ImportButton', () => {
  it('hatalı raporda satırları gösteriyor ve içe aktarmaya izin vermiyor', async () => {
    uploadImport.mockResolvedValue({
      dryRun: true, totalRows: 2, validRows: 1, created: 0,
      errors: [{ row: 3, column: 'Ortam', message: 'Geçersiz ortam: CANLI' }],
    })
    renderButton()
    fireEvent.click(screen.getByText('Kontrol et'))

    expect(await screen.findByText(/Satır 3 · Ortam: Geçersiz ortam: CANLI/)).toBeTruthy()
    expect((screen.getByText('İçe aktar') as HTMLButtonElement).disabled).toBe(true)
    expect(uploadImport).toHaveBeenCalledWith('/servers/import', expect.any(File), true)
  })

  it('temiz kontrolden sonra aynı dosyayı gerçekten yazıyor', async () => {
    uploadImport
      .mockResolvedValueOnce({ dryRun: true, totalRows: 2, validRows: 2, created: 0, errors: [] })
      .mockResolvedValueOnce({ dryRun: false, totalRows: 2, validRows: 2, created: 2, errors: [] })
    renderButton()
    fireEvent.click(screen.getByText('Kontrol et'))
    await screen.findByText(/2 satır hatasız/)

    fireEvent.click(screen.getByText('İçe aktar'))
    await waitFor(() => expect(screen.getByText('2 kayıt eklendi.')).toBeTruthy())
    expect(uploadImport).toHaveBeenLastCalledWith('/servers/import', expect.any(File), false)
  })
})
