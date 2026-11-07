import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import ErrorBoundary from './ErrorBoundary'

function Rompe() {
  throw new Error('boom')
}

describe('ErrorBoundary', () => {
  it('muestra una salida controlada ante un error de render', () => {
    vi.spyOn(console, 'error').mockImplementation(() => {})
    render(<ErrorBoundary><Rompe /></ErrorBoundary>)
    expect(screen.getByRole('heading', { name: 'Algo salió mal' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Recargar' })).toBeInTheDocument()
  })
})
