import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Reservas_detalle from './Reservas_detalle'
import { obtenerDashboard } from '../services/bffService'

vi.mock('../services/bffService', () => ({
  obtenerDashboard: vi.fn(),
}))

describe('Reservas_detalle', () => {
  beforeEach(() => {
    localStorage.setItem('ID_R', '8')
    obtenerDashboard.mockResolvedValue({
      reservas: {
        datos: [{
          id: 8,
          fechaReserva: '2026-11-10',
          fechaTermino: '2026-11-12',
          tipoReserva: 'Doble',
          cantidadPersonas: 2,
          valorFinal: 120000,
        }],
      },
    })
  })

  it('muestra la reserva solicitada', async () => {
    render(
      <MemoryRouter>
        <Reservas_detalle />
      </MemoryRouter>,
    )
    expect(await screen.findByText('Detalle de reserva')).toBeInTheDocument()
    expect(screen.getByText('2026-11-10')).toBeInTheDocument()
    expect(screen.getByText('120000')).toBeInTheDocument()
  })

  it('muestra error si no encuentra la reserva', async () => {
    localStorage.setItem('ID_R', '99')
    render(
      <MemoryRouter>
        <Reservas_detalle />
      </MemoryRouter>,
    )
    expect(await screen.findByText('No se encontró la reserva solicitada')).toBeInTheDocument()
  })
})
