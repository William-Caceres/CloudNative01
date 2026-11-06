import { describe, expect, it } from 'vitest'
import { formatearPrecio } from './format'

describe('formatearPrecio', () => {
  it('formatea un precio en pesos chilenos', () => {
    expect(formatearPrecio(250000)).toBe('250.000')
  })

  it('formatea cero', () => {
    expect(formatearPrecio(0)).toBe('0')
  })
})
