import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Servicios from './Servicios'
import { listarServicios } from '../services/servicioService'

vi.mock('../services/servicioService', () => ({
  listarServicios: vi.fn(),
  obtenerServicio: vi.fn(),
  eliminarServicio: vi.fn(),
}))

function renderServicios() {
  return render(
    <MemoryRouter>
      <Servicios />
    </MemoryRouter>,
  )
}

describe('Servicios', () => {
  beforeEach(() => {
    listarServicios.mockResolvedValue([
      { id: 1, nombre: 'Spa', descripcion: 'Relajación', precio: 50000, capacidad: 1 },
    ])
  })

  it('muestra el catálogo cargado', async () => {
    renderServicios()
    expect(await screen.findByRole('heading', { name: 'Spa' })).toBeInTheDocument()
  })

  it('muestra estado vacío', async () => {
    listarServicios.mockResolvedValue([])
    renderServicios()
    expect(await screen.findByText('No hay servicios disponibles.')).toBeInTheDocument()
  })

  it('muestra error de carga', async () => {
    listarServicios.mockRejectedValue(new Error('sin conexión'))
    renderServicios()
    expect(await screen.findByText(/No se pudieron cargar los servicios/)).toBeInTheDocument()
  })
})
