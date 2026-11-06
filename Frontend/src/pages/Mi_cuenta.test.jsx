import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Mi_cuenta from './Mi_cuenta'
import { obtenerDashboard } from '../services/bffService'

const auth = vi.hoisted(() => ({
  authenticated: false,
  account: null,
  instance: null,
}))

vi.mock('@azure/msal-react', () => ({
  useMsal: () => ({ instance: auth.instance }),
  useIsAuthenticated: () => auth.authenticated,
  useAccount: () => auth.account,
}))

vi.mock('../auth/authUtils', () => ({
  getRoles: () => ['Admin'],
  getAccessTokenScopes: vi.fn().mockResolvedValue(['User.Read']),
}))

vi.mock('../services/bffService', () => ({
  obtenerDashboard: vi.fn(),
}))

describe('Mi_cuenta', () => {
  beforeEach(() => {
    auth.authenticated = false
    auth.account = null
    auth.instance = { getAllAccounts: () => auth.account ? [auth.account] : [] }
    obtenerDashboard.mockResolvedValue({
      usuario: { datos: { id: 1, nombre: 'Emilio' } },
      reservas: { datos: [] },
      servicios: { datos: [{ id: 1 }, { id: 2 }] },
    })
  })

  it('pide iniciar sesión sin autenticación', () => {
    render(<Mi_cuenta />)
    expect(screen.getByText('Debes iniciar sesión para ver tu cuenta.')).toBeInTheDocument()
  })

  it('muestra errores del resumen sin romper la cuenta', async () => {
    auth.authenticated = true
    auth.account = { username: 'admin@hotel.test', name: 'Emilio', idTokenClaims: { roles: ['Admin'] } }
    obtenerDashboard.mockResolvedValue({
      usuario: { error: 'usuario caído' },
      reservas: { error: 'reservas caídas' },
      servicios: { error: 'servicios caídos' },
    })
    render(<Mi_cuenta />)
    expect(await screen.findByText('Error: usuario caído')).toBeInTheDocument()
    expect(screen.getByText('Error: reservas caídas')).toBeInTheDocument()
    expect(screen.getByText('Error: servicios caídos')).toBeInTheDocument()
  })

  it('muestra perfil, rol, scopes y resumen', async () => {
    auth.authenticated = true
    auth.account = {
      username: 'admin@hotel.test',
      name: 'Emilio',
      idTokenClaims: { name: 'Emilio', roles: ['Admin'] },
    }
    render(<Mi_cuenta />)
    expect(await screen.findByText('admin@hotel.test')).toBeInTheDocument()
    expect(screen.getByText('Admin')).toBeInTheDocument()
    expect(await screen.findByText(/User.Read/)).toBeInTheDocument()
    expect(await screen.findByText(/Emilio \(id 1\)/)).toBeInTheDocument()
    expect(screen.getByText('2 servicios')).toBeInTheDocument()
  })
})
