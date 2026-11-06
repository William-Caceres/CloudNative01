import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Reservas from './Reservas'
import { obtenerDashboard } from '../services/bffService'

vi.mock('../services/bffService', () => ({
  obtenerDashboard: vi.fn(),
}))

function renderReservas() {
  return render(
    <MemoryRouter>
      <Reservas />
    </MemoryRouter>,
  )
}

describe('Reservas', () => {
  beforeEach(() => {
    obtenerDashboard.mockResolvedValue({
      reservas: {
        datos: [{
          id: 8,
          fechaReserva: '2026-11-10',
          fechaTermino: '2026-11-12',
          tipoReserva: 'Doble',
          cantidadPersonas: 2,
          valorFinal: 120000,
          estadoPago: 'PENDIENTE',
        }],
      },
    })
  })

  it('muestra las reservas del dashboard', async () => {
    renderReservas()
    expect(await screen.findByText('Reserva #8')).toBeInTheDocument()
    expect(screen.getByText('2026-11-10')).toBeInTheDocument()
  })

  it('guarda el id de la reserva para el detalle', async () => {
    const user = userEvent.setup()
    renderReservas()
    await screen.findByText('Reserva #8')
    await user.click(screen.getByRole('button', { name: 'Ver detalle' }))
    expect(localStorage.getItem('ID_R')).toBe('8')
  })

  it('muestra estado vacío', async () => {
    obtenerDashboard.mockResolvedValue({ reservas: { datos: [] } })
    renderReservas()
    expect(await screen.findByText('No tienes reservas registradas.')).toBeInTheDocument()
  })

  it('muestra error del dashboard', async () => {
    obtenerDashboard.mockRejectedValue(new Error('sin conexión'))
    renderReservas()
    expect(await screen.findByText(/No se pudieron cargar las reservas/)).toBeInTheDocument()
  })
})
