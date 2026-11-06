import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Modificar_servicios from './Modificar_servicios'
import { modificarServicio, obtenerServicio } from '../services/servicioService'

vi.mock('../services/servicioService', () => ({
  obtenerServicio: vi.fn(),
  modificarServicio: vi.fn(),
}))

describe('Modificar_servicios', () => {
  const show = vi.fn()

  beforeEach(() => {
    show.mockReset()
    vi.spyOn(window, 'alert').mockImplementation(() => {})
    localStorage.setItem('ID_S', '4')
    obtenerServicio.mockResolvedValue({
      tipoServicio: 'habitacion',
      precio: 120000,
      numHabitacion: 4,
      capacidad: 2,
      disponible: true,
      nivelServicio: 'VIP',
      nombre: 'Suite Real',
      descripcion: 'Vista al mar',
    })
    modificarServicio.mockResolvedValue({})
  })

  it('carga el servicio y guarda los cambios', async () => {
    const user = userEvent.setup()
    render(<Modificar_servicios show={show} />)

    expect(await screen.findByDisplayValue('Suite Real')).toBeInTheDocument()
    await user.selectOptions(screen.getByLabelText(/Tipo de servicio/), 'comida')
    await user.selectOptions(screen.getByLabelText(/Nivel del servicio/), 'miembro')
    await user.clear(screen.getByLabelText(/Nombre:/))
    await user.type(screen.getByLabelText(/Nombre:/), 'Suite Real')
    await user.clear(screen.getByLabelText(/Descripción:/))
    await user.type(screen.getByLabelText(/Descripción:/), 'Vista al mar')
    await user.click(screen.getByLabelText(/Está disponible/))
    await user.click(screen.getByRole('button', { name: 'Modificar' }))

    expect(modificarServicio).toHaveBeenCalledWith('4', expect.objectContaining({
      nombre: 'Suite Real',
      nivelServicio: 'miembro',
    }))
    expect(show).toHaveBeenCalledWith(false)
  })
})
