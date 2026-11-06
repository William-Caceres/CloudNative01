import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import App from './App'

vi.mock('./pages/Navbar', () => ({ default: () => <div>Navbar mock</div> }))
vi.mock('./routes/RouterConfig', () => ({ default: () => <div>Rutas mock</div> }))

describe('App', () => {
  it('compone navbar y rutas', () => {
    render(<App />)
    expect(screen.getByText('Navbar mock')).toBeInTheDocument()
    expect(screen.getByText('Rutas mock')).toBeInTheDocument()
  })
})
