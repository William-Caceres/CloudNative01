import { beforeEach, describe, expect, it, vi } from 'vitest'

const axiosMocks = vi.hoisted(() => ({
  request: vi.fn(),
  capture: null,
}))

const msal = vi.hoisted(() => ({
  getAllAccounts: vi.fn(() => []),
}))

const auth = vi.hoisted(() => ({
  getAccessToken: vi.fn(),
}))

vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      interceptors: { request: { use: (fn) => { axiosMocks.capture = fn } } },
      request: axiosMocks.request,
    })),
  },
}))

vi.mock('./msalInstance', () => ({ msalInstance: msal }))
vi.mock('./authUtils', () => ({ getAccessToken: auth.getAccessToken }))

const { apiRequest } = await import('./apiClient')

describe('apiClient', () => {
  beforeEach(() => {
    axiosMocks.request.mockResolvedValue({ data: { ok: true } })
    msal.getAllAccounts.mockReturnValue([])
    auth.getAccessToken.mockReset()
  })

  it('devuelve el data de la respuesta', async () => {
    await expect(apiRequest({ baseURL: 'https://api.test', url: '/x' })).resolves.toEqual({ ok: true })
  })

  it('deja pasar la petición sin cuentas', async () => {
    const interceptor = axiosMocks.capture
    const config = { headers: {} }
    await expect(interceptor(config)).resolves.toBe(config)
    expect(auth.getAccessToken).not.toHaveBeenCalled()
  })

  it('agrega el bearer cuando hay cuenta', async () => {
    msal.getAllAccounts.mockReturnValue([{ username: 'admin@hotel.test' }])
    auth.getAccessToken.mockResolvedValue('token-123')
    const interceptor = axiosMocks.capture
    const config = { headers: {} }

    await interceptor(config)

    expect(config.headers.Authorization).toBe('Bearer token-123')
  })
})
