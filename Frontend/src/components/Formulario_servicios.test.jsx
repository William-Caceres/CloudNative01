import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Formulario_servicios from './Formulario_servicios'
import { registrarServicio } from '../services/servicioService'

vi.mock('../services/servicioService', () => ({
  registrarServicio: vi.fn(),
}))

describe('Formulario_servicios', () => {
  const show = vi.fn()

  beforeEach(() => {
    registrarServicio.mockResolvedValue({})
    show.mockReset()
    vi.spyOn(window, 'alert').mockImplementation(() => {})
  })

  it('rechaza un formulario con precio y capacidad inválidos', async () => {
    const user = userEvent.setup()
    render(<Formulario_servicios show={show} />)

    await user.selectOptions(screen.getByLabelText(/Tipo de servicio/), 'habitacion')
    await user.selectOptions(screen.getByLabelText(/Nivel del servicio/), 'VIP')
    await user.type(screen.getByLabelText(/Nombre:/), 'Suite Real')
    await user.type(screen.getByLabelText(/Descripción:/), 'Vista al mar')
    await user.click(screen.getByRole('button', { name: 'Registrar' }))

    expect(window.alert).toHaveBeenCalled()
    expect(registrarServicio).not.toHaveBeenCalled()
  })

  it('registra un servicio válido y cierra el formulario', async () => {
    const user = userEvent.setup()
    render(<Formulario_servicios show={show} />)

    await user.clear(screen.getByLabelText(/ID de servicio/))
    await user.type(screen.getByLabelText(/ID de servicio/), '12')
    await user.selectOptions(screen.getByLabelText(/Tipo de servicio/), 'habitacion')
    await user.selectOptions(screen.getByLabelText(/Nivel del servicio/), 'VIP')
    await user.type(screen.getByLabelText(/Nombre:/), 'Suite Real')
    await user.type(screen.getByLabelText(/Descripción:/), 'Vista al mar')
    await user.type(screen.getByLabelText(/Precio:/), '150000')
    await user.type(screen.getByLabelText(/Capacidad servicio/), '2')
    await user.click(screen.getByRole('button', { name: 'Registrar' }))

    expect(registrarServicio).toHaveBeenCalledWith(expect.objectContaining({
      tipoServicio: 'habitacion',
      nivelServicio: 'VIP',
      nombre: 'Suite Real',
      precio: '150000',
      capacidad: '2',
    }))
    expect(show).toHaveBeenCalledWith(false)
  })
})
