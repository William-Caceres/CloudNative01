import { apiRequest } from '../auth/apiClient'
import { environment } from '../utils/environment'

const BASE = environment.apiBaseUrl

export async function obtenerDashboard() {
    return apiRequest({ baseURL: BASE, url: '/api/v1/bff/dashboard' })
}
