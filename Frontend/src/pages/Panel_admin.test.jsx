import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Panel_admin from './Panel_admin'
import { listarServicios } from '../services/servicioService'

vi.mock('../services/servicioService', () => ({
  listarServicios: vi.fn(),
  obtenerServicio: vi.fn(),
  eliminarServicio: vi.fn(),
  registrarServicio: vi.fn(),
}))

function renderPanel() {
  return render(
    <MemoryRouter>
      <Panel_admin />
    </MemoryRouter>,
  )
}

describe('Panel_admin', () => {
  beforeEach(() => {
    listarServicios.mockResolvedValue([
      { id: 1, nombre: 'Spa', descripcion: 'Relajación', precio: 50000, capacidad: 1 },
    ])
  })

  it('muestra servicios y el botón de registro', async () => {
    renderPanel()
    expect(await screen.findByRole('heading', { name: 'Spa' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Registrar servicio' })).toBeInTheDocument()
  })

  it('abre el formulario de registro', async () => {
    const user = userEvent.setup()
    renderPanel()
    await user.click(screen.getByRole('button', { name: 'Registrar servicio' }))
    expect(screen.getByRole('button', { name: 'Cerrar' })).toBeInTheDocument()
  })
})
