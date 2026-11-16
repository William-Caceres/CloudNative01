const account = {
  username: 'admin@e2e.test',
  name: 'Admin E2E',
  idTokenClaims: { name: 'Admin E2E', roles: ['Admin'] },
}

function authEnabled() {
  return localStorage.getItem('e2e-auth') !== 'off'
}

function fakeToken() {
  const payload = btoa(JSON.stringify({ scp: 'User.Read' }))
    .replaceAll('+', '-')
    .replaceAll('/', '_')
    .replaceAll('=', '')
  return `header.${payload}.signature`
}

export const e2eAccount = account

export const msalInstance = {
  initialize: async () => {},
  getAllAccounts: () => authEnabled() ? [account] : [],
  acquireTokenSilent: async () => ({ accessToken: fakeToken() }),
  loginRedirect: async () => {},
  logoutRedirect: async () => {},
}
