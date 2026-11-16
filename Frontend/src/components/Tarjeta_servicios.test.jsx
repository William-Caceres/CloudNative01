import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Tarjeta_servicios from './Tarjeta_servicios'
import { eliminarServicio, obtenerServicio } from '../services/servicioService'

vi.mock('../services/servicioService', () => ({
  obtenerServicio: vi.fn(),
  eliminarServicio: vi.fn(),
}))

const servicio = {
  id: 4,
  nombre: 'Suite Presidencial',
  descripcion: 'Vista al jardín',
  precio: 250000,
  capacidad: 2,
}

function renderCard(admin = false) {
  return render(
    <MemoryRouter>
      <Tarjeta_servicios servicio={servicio} admin={admin} />
    </MemoryRouter>,
  )
}

describe('Tarjeta_servicios', () => {
  beforeEach(() => {
    eliminarServicio.mockResolvedValue(true)
    obtenerServicio.mockResolvedValue(servicio)
    vi.spyOn(window, 'confirm').mockReturnValue(true)
  })

  it('muestra los datos del servicio', () => {
    renderCard()
    expect(screen.getByRole('heading', { name: 'Suite Presidencial' })).toBeInTheDocument()
    expect(screen.getByText('Vista al jardín')).toBeInTheDocument()
    expect(screen.getByText(/\$250\.000/)).toBeInTheDocument()
  })

  it('guarda el id y navega al detalle', async () => {
    const user = userEvent.setup()
    renderCard()
    await user.click(screen.getByRole('button', { name: 'Ver detalles' }))
    expect(localStorage.getItem('ID_S')).toBe('4')
  })

  it('muestra acciones de administración solo con admin', () => {
    const { unmount } = renderCard()
    expect(screen.queryByRole('button', { name: 'Modificar' })).not.toBeInTheDocument()
    unmount()
    renderCard(true)
    expect(screen.getByRole('button', { name: 'Modificar' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Eliminar' })).toBeInTheDocument()
  })

  it('elimina el servicio y oculta la tarjeta', async () => {
    const user = userEvent.setup()
    renderCard(true)
    await user.click(screen.getByRole('button', { name: 'Eliminar' }))
    expect(eliminarServicio).toHaveBeenCalledWith(4)
    expect(screen.queryByRole('heading', { name: 'Suite Presidencial' })).not.toBeInTheDocument()
  })
})
