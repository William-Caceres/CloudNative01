import { describe, expect, it, vi } from 'vitest'
import { getAccessToken, getAccessTokenScopes, getRoles } from './authUtils'

function fakeJwt(payload) {
  const body = btoa(JSON.stringify(payload)).replaceAll('+', '-').replaceAll('/', '_').replaceAll('=', '')
  return `header.${body}.signature`
}

function account(roles = ['Admin']) {
  return { idTokenClaims: { roles } }
}

describe('authUtils', () => {
  it('devuelve los roles del idToken', () => {
    expect(getRoles(account(['Admin', 'Cliente']))).toEqual(['Admin', 'Cliente'])
  })

  it('devuelve arreglo vacío si no hay roles válidos', () => {
    expect(getRoles(null)).toEqual([])
    expect(getRoles({ idTokenClaims: { roles: 'Admin' } })).toEqual([])
  })

  it('obtiene scopes desde el access token', async () => {
    const instance = {
      getAllAccounts: () => [account()],
      acquireTokenSilent: vi.fn().mockResolvedValue({ accessToken: fakeJwt({ scp: 'User.Read Mail.Read' }) }),
    }

    await expect(getAccessTokenScopes(instance)).resolves.toEqual(['User.Read', 'Mail.Read'])
  })

  it('devuelve scopes vacíos sin cuentas', async () => {
    const instance = { getAllAccounts: () => [] }
    await expect(getAccessTokenScopes(instance)).resolves.toEqual([])
  })

  it('obtiene token silencioso', async () => {
    const instance = {
      getAllAccounts: () => [account()],
      acquireTokenSilent: vi.fn().mockResolvedValue({ accessToken: 'token-silencioso' }),
    }

    await expect(getAccessToken(instance)).resolves.toBe('token-silencioso')
  })

  it('hace redirect si falla el token silencioso', async () => {
    const instance = {
      getAllAccounts: () => [account()],
      acquireTokenSilent: vi.fn().mockRejectedValue(new Error('expirado')),
      acquireTokenRedirect: vi.fn().mockResolvedValue(null),
    }

    await expect(getAccessToken(instance)).resolves.toBeNull()
    expect(instance.acquireTokenRedirect).toHaveBeenCalledOnce()
  })

  it('falla sin sesión', async () => {
    const instance = { getAllAccounts: () => [] }
    await expect(getAccessToken(instance)).rejects.toThrow('Sin sesión')
  })
})
