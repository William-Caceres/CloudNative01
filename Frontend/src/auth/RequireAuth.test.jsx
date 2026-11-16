import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import RequireAuth from './RequireAuth'

const auth = vi.hoisted(() => ({
  authenticated: false,
  accounts: [],
  instance: null,
}))

vi.mock('@azure/msal-react', () => ({
  useMsal: () => ({ instance: auth.instance }),
  useIsAuthenticated: () => auth.authenticated,
}))

function renderProtected(roles) {
  return render(
    <MemoryRouter initialEntries={['/privado']}>
      <Routes>
        <Route path="/privado" element={<RequireAuth roles={roles}><div>Contenido privado</div></RequireAuth>} />
        <Route path="/login" element={<div>Pantalla login</div>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('RequireAuth', () => {
  beforeEach(() => {
    auth.authenticated = false
    auth.accounts = []
    auth.instance = { getAllAccounts: () => auth.accounts }
  })

  it('redirige a login sin sesión', () => {
    renderProtected()
    expect(screen.getByText('Pantalla login')).toBeInTheDocument()
  })

  it('muestra el contenido con sesión válida', () => {
    auth.authenticated = true
    auth.accounts = [{ idTokenClaims: { roles: ['Cliente'] } }]
    renderProtected()
    expect(screen.getByText('Contenido privado')).toBeInTheDocument()
  })

  it('muestra acceso denegado si falta el rol', () => {
    auth.authenticated = true
    auth.accounts = [{ idTokenClaims: { roles: ['Cliente'] } }]
    renderProtected(['Admin'])
    expect(screen.getByText('Acceso denegado')).toBeInTheDocument()
  })

  it('espera la restauración de sesión', () => {
    auth.accounts = [{ idTokenClaims: { roles: ['Admin'] } }]
    const { container } = renderProtected()
    expect(container).toBeEmptyDOMElement()
  })
})
