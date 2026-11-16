import { apiRequest } from '../auth/apiClient'
import { environment } from '../utils/environment'

const BASE = environment.apiBaseUrl

export async function obtenerResumenKafka() {
    return apiRequest({ baseURL: BASE, url: '/api/v1/broker-admin/kafka/summary' })
}

export async function obtenerCola(nombre) {
    return apiRequest({ baseURL: BASE, url: `/api/v1/broker-admin/queues/${nombre}` })
}
