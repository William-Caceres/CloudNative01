import axios from 'axios'
import { environment } from '../utils/environment'
import { msalInstance } from './msalInstance'
import { getAccessToken } from './authUtils'

const apiClient = axios.create({
    baseURL: `${environment.apiBaseUrl}/api/v1/usuario`,
})

apiClient.interceptors.request.use(async (config) => {
    if (msalInstance.getAllAccounts().length === 0) return config
    const token = await getAccessToken(msalInstance)
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

export async function apiRequest({ baseURL, url, method = 'get', data }) {
    const response = await apiClient.request({ baseURL, url, method, data })
    return response.data
}
