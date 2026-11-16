import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import { fileURLToPath } from 'node:url'
import { resolve } from 'node:path'

const root = fileURLToPath(new URL('.', import.meta.url))

export default defineConfig({
  plugins: [react()],
  define: {
    'import.meta.env.VITE_API_BASE_URL': JSON.stringify('https://api.e2e.invalid'),
  },
  resolve: {
    alias: [
      { find: '@azure/msal-react', replacement: resolve(root, 'src/test/e2e/msalReactMock.jsx') },
      { find: resolve(root, 'src/auth/msalInstance.js'), replacement: resolve(root, 'src/test/e2e/msalInstanceMock.js') },
    ],
  },
  server: {
    host: '127.0.0.1',
    port: 4174,
  },
})
