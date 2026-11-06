import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import Inicio from './Inicio'

function renderInicio() {
  return render(
    <MemoryRouter initialEntries={['/']}>
      <Routes>
        <Route path="/" element={<Inicio />} />
        <Route path="/servicios" element={<div>Destino servicios</div>} />
        <Route path="/mis_reservas" element={<div>Destino reservas</div>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('Inicio', () => {
  it('muestra la bienvenida y navega a servicios', async () => {
    const user = userEvent.setup()
    renderInicio()
    expect(screen.getByText(/Bienvenido a Grand Hotel/)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Servicios' }))
    expect(screen.getByText('Destino servicios')).toBeInTheDocument()
  })

  it('navega a mis reservas', async () => {
    const user = userEvent.setup()
    renderInicio()
    await user.click(screen.getByRole('button', { name: 'Mis reservas' }))
    expect(screen.getByText('Destino reservas')).toBeInTheDocument()
  })
})
