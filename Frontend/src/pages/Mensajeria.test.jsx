import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Mensajeria from './Mensajeria'
import { obtenerCola, obtenerResumenKafka } from '../services/brokerAdminService'

vi.mock('../services/brokerAdminService', () => ({
  obtenerResumenKafka: vi.fn(),
  obtenerCola: vi.fn(),
}))

describe('Mensajeria', () => {
  beforeEach(() => {
    obtenerResumenKafka.mockResolvedValue({
      brokers: 3,
      topics: 4,
      grupos: 2,
      lagTotal: 0,
      reservasProcesadas: 7,
      pagosProcesados: 5,
      detalleGrupos: [{ grupo: 'analytics-group', estado: 'STABLE', miembros: 3, lagTotal: 0 }],
    })
    obtenerCola.mockImplementation((nombre) => Promise.resolve({ nombre, mensajes: 0, consumidores: 1 }))
  })

  it('muestra el error de mensajeria sin romper la pagina', async () => {
    obtenerResumenKafka.mockRejectedValueOnce(new Error('broker caido'))
    render(
      <MemoryRouter>
        <Mensajeria />
      </MemoryRouter>,
    )
    expect(await screen.findByText(/No se pudo leer la mensajer/)).toBeInTheDocument()
  })

  it('muestra el estado de espera antes de recibir datos', () => {
    obtenerResumenKafka.mockReturnValue(new Promise(() => {}))
    obtenerCola.mockReturnValue(new Promise(() => {}))
    render(
      <MemoryRouter>
        <Mensajeria />
      </MemoryRouter>,
    )
    expect(screen.getByText('Conectando')).toBeInTheDocument()
    expect(screen.getAllByText('Esperando datos').length).toBeGreaterThan(0)
  })

  it('muestra el estado de Kafka y las colas de Rabbit', async () => {
    render(
      <MemoryRouter>
        <Mensajeria />
      </MemoryRouter>,
    )
    expect(await screen.findByText('Notificaciones')).toBeInTheDocument()
    expect(screen.getByText('analytics-group')).toBeInTheDocument()
    expect(screen.getByText('Reservas procesadas')).toBeInTheDocument()
  })
})
