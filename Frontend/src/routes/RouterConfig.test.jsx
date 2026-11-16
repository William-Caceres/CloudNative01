import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import RouterConfig from './RouterConfig'

vi.mock('../pages/Inicio', () => ({ default: () => <div>Inicio mock</div> }))
vi.mock('../pages/Login', () => ({ default: () => <div>Login mock</div> }))
vi.mock('../pages/Mi_cuenta', () => ({ default: () => <div>Cuenta mock</div> }))
vi.mock('../pages/Servicios', () => ({ default: () => <div>Servicios mock</div> }))
vi.mock('../pages/Servicios_detalle', () => ({ default: () => <div>Detalle servicio mock</div> }))
vi.mock('../pages/Reservas', () => ({ default: () => <div>Reservas mock</div> }))
vi.mock('../pages/Reservas_detalle', () => ({ default: () => <div>Detalle reserva mock</div> }))
vi.mock('../pages/Panel_admin', () => ({ default: () => <div>Admin mock</div> }))
vi.mock('../auth/RequireAuth', () => ({ default: ({ children }) => <>{children}</> }))

describe('RouterConfig', () => {
  it.each([
    ['/', 'Inicio mock'],
    ['/login', 'Login mock'],
    ['/mi_cuenta', 'Cuenta mock'],
    ['/servicios', 'Servicios mock'],
    ['/servicios/detalle', 'Detalle servicio mock'],
    ['/mis_reservas', 'Reservas mock'],
    ['/mis_reservas/detalle', 'Detalle reserva mock'],
    ['/PanelAdministradores', 'Admin mock'],
  ])('resuelve la ruta %s', (path, text) => {
    render(
      <MemoryRouter initialEntries={[path]}>
        <RouterConfig />
      </MemoryRouter>,
    )
    expect(screen.getByText(text)).toBeInTheDocument()
  })
})
