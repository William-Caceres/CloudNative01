/* oxlint-disable react/only-export-components */
import { e2eAccount, msalInstance } from './msalInstanceMock'

function authEnabled() {
  return localStorage.getItem('e2e-auth') !== 'off'
}

export function MsalProvider({ children }) {
  return children
}

export function useMsal() {
  return { instance: msalInstance }
}

export function useIsAuthenticated() {
  return authEnabled()
}

export function useAccount() {
  return authEnabled() ? e2eAccount : null
}
