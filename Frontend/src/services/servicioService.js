import { apiRequest } from '../auth/apiClient'
import { environment } from '../utils/environment'

const BASE = environment.apiBaseUrl

export async function listarServicios() {
    return apiRequest({ baseURL: BASE, url: '/api/v1/servicio/list' })
}

export async function obtenerServicio(id) {
    return apiRequest({ baseURL: BASE, url: `/api/v1/servicio/get/${id}` })
}

export async function registrarServicio(servicio) {
    return apiRequest({ baseURL: BASE, url: '/api/v1/servicio/post', method: 'post', data: servicio })
}

export async function modificarServicio(id, servicio) {
    return apiRequest({ baseURL: BASE, url: `/api/v1/servicio/put/${id}`, method: 'put', data: servicio })
}

export async function eliminarServicio(id) {
    return apiRequest({ baseURL: BASE, url: `/api/v1/servicio/delete/${id}`, method: 'delete' })
}
