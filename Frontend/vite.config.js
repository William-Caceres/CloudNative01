import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  if (mode === 'production' && !env.VITE_API_BASE_URL) {
    throw new Error('VITE_API_BASE_URL es obligatorio para el build de producción. Usar: terraform -chdir=infra output -raw api_url')
  }

  return {
    plugins: [react()],
  }
})
