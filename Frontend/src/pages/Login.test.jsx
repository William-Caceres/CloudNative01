import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import Login from './Login'

const auth = vi.hoisted(() => ({
  authenticated: false,
  accounts: [],
  instance: null,
}))

vi.mock('@azure/msal-react', () => ({
  useMsal: () => ({ instance: auth.instance }),
  useIsAuthenticated: () => auth.authenticated,
  useAccount: () => auth.accounts[0] || null,
}))

function fakeJwt(payload) {
  const body = btoa(JSON.stringify(payload)).replaceAll('+', '-').replaceAll('/', '_').replaceAll('=', '')
  return `header.${body}.signature`
}

describe('Login', () => {
  beforeEach(() => {
    auth.authenticated = false
    auth.accounts = []
    auth.instance = {
      getAllAccounts: () => auth.accounts,
      acquireTokenSilent: vi.fn().mockResolvedValue({ accessToken: fakeJwt({ scp: 'User.Read' }) }),
      loginRedirect: vi.fn(),
      logoutRedirect: vi.fn(),
    }
  })

  it('muestra el acceso cuando no hay sesión', () => {
    render(<Login />)
    expect(screen.getByRole('heading', { name: 'Iniciar sesión' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Iniciar sesión' })).toBeInTheDocument()
  })

  it('dispara el login redirect', async () => {
    const user = userEvent.setup()
    render(<Login />)
    await user.click(screen.getByRole('button', { name: 'Iniciar sesión' }))
    expect(auth.instance.loginRedirect).toHaveBeenCalledOnce()
  })

  it('dispara el logout redirect', async () => {
    const user = userEvent.setup()
    auth.authenticated = true
    auth.accounts = [{ username: 'admin@hotel.test', idTokenClaims: { roles: ['Admin'] } }]
    render(<Login />)
    await screen.findByText('Sesión activa')
    await user.click(screen.getByRole('button', { name: 'Cerrar sesión' }))
    expect(auth.instance.logoutRedirect).toHaveBeenCalledOnce()
  })

  it('muestra la sesión activa y los scopes', async () => {
    auth.authenticated = true
    auth.accounts = [{ username: 'admin@hotel.test', idTokenClaims: { roles: ['Admin'] } }]
    render(<Login />)
    expect(await screen.findByText('Sesión activa')).toBeInTheDocument()
    expect(screen.getByText(/admin@hotel.test/)).toBeInTheDocument()
    expect(await screen.findByText(/User.Read/)).toBeInTheDocument()
  })
})
