import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Navbar from './Navbar'

const auth = vi.hoisted(() => ({
  authenticated: false,
  accounts: [],
  instance: null,
}))

vi.mock('@azure/msal-react', () => ({
  useMsal: () => ({ instance: auth.instance }),
  useIsAuthenticated: () => auth.authenticated,
}))

function renderNavbar() {
  return render(
    <MemoryRouter>
      <Navbar />
    </MemoryRouter>,
  )
}

describe('Navbar', () => {
  beforeEach(() => {
    auth.authenticated = false
    auth.accounts = []
    auth.instance = {
      getAllAccounts: () => auth.accounts,
      logoutRedirect: vi.fn(),
    }
  })

  it('muestra iniciar sesión sin autenticación', () => {
    renderNavbar()
    expect(screen.getByText('Iniciar sesión')).toBeInTheDocument()
    expect(screen.queryByText('Panel admin')).not.toBeInTheDocument()
  })

  it('muestra navegación de cliente sin panel admin', () => {
    auth.authenticated = true
    auth.accounts = [{ idTokenClaims: { roles: ['Cliente'] } }]
    renderNavbar()
    expect(screen.getByText('Mis Reservas')).toBeInTheDocument()
    expect(screen.queryByText('Panel admin')).not.toBeInTheDocument()
  })

  it('muestra panel admin para rol Admin', () => {
    auth.authenticated = true
    auth.accounts = [{ idTokenClaims: { roles: ['Admin'] } }]
    renderNavbar()
    expect(screen.getByText('Panel admin')).toBeInTheDocument()
  })

  it('cierra sesión desde el navbar', async () => {
    const user = userEvent.setup()
    auth.authenticated = true
    auth.accounts = [{ idTokenClaims: { roles: ['Cliente'] } }]
    renderNavbar()
    await user.click(screen.getByText('Cerrar sesión'))
    expect(auth.instance.logoutRedirect).toHaveBeenCalledOnce()
  })
})
