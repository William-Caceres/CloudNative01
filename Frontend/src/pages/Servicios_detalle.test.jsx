import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Servicios_detalle from './Servicios_detalle'
import { obtenerServicio } from '../services/servicioService'

vi.mock('../services/servicioService', () => ({
  obtenerServicio: vi.fn(),
}))

describe('Servicios_detalle', () => {
  beforeEach(() => {
    localStorage.setItem('ID_S', '4')
    obtenerServicio.mockResolvedValue({
      nombre: 'Suite Real',
      descripcion: 'Vista al mar',
      numHabitacion: 4,
      tipoServicio: 'habitacion',
      nivelServicio: 'VIP',
      disponible: true,
      precio: 120000,
      capacidad: 2,
    })
  })

  it('muestra el detalle solicitado', async () => {
    render(
      <MemoryRouter>
        <Servicios_detalle />
      </MemoryRouter>,
    )
    expect(await screen.findByText('Suite Real')).toBeInTheDocument()
    expect(screen.getByText('Vista al mar')).toBeInTheDocument()
    expect(screen.getByText(/Está disponible/)).toBeInTheDocument()
  })

  it('muestra un servicio no disponible', async () => {
    obtenerServicio.mockResolvedValue({
      nombre: 'Spa',
      descripcion: 'En mantenimiento',
      numHabitacion: 2,
      tipoServicio: 'recreativo',
      nivelServicio: 'Invitado',
      disponible: false,
      precio: 30000,
      capacidad: 1,
    })
    render(
      <MemoryRouter>
        <Servicios_detalle />
      </MemoryRouter>,
    )
    expect(await screen.findByText('No disponible...')).toBeInTheDocument()
  })
})
