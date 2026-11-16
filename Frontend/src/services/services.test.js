import { beforeEach, describe, expect, it, vi } from 'vitest'
import { apiRequest } from '../auth/apiClient'
import { obtenerDashboard } from './bffService'
import {
  eliminarServicio,
  listarServicios,
  modificarServicio,
  obtenerServicio,
  registrarServicio,
} from './servicioService'

vi.mock('../auth/apiClient', () => ({
  apiRequest: vi.fn(),
}))

describe('servicios', () => {
  beforeEach(() => {
    apiRequest.mockResolvedValue({})
  })

  it('arma las rutas de servicios', async () => {
    const body = { nombre: 'Spa' }
    await listarServicios()
    await obtenerServicio(4)
    await registrarServicio(body)
    await modificarServicio(4, body)
    await eliminarServicio(4)

    expect(apiRequest).toHaveBeenNthCalledWith(1, expect.objectContaining({ url: '/api/v1/servicio/list' }))
    expect(apiRequest).toHaveBeenNthCalledWith(2, expect.objectContaining({ url: '/api/v1/servicio/get/4' }))
    expect(apiRequest).toHaveBeenNthCalledWith(3, expect.objectContaining({ url: '/api/v1/servicio/post', method: 'post', data: body }))
    expect(apiRequest).toHaveBeenNthCalledWith(4, expect.objectContaining({ url: '/api/v1/servicio/put/4', method: 'put', data: body }))
    expect(apiRequest).toHaveBeenNthCalledWith(5, expect.objectContaining({ url: '/api/v1/servicio/delete/4', method: 'delete' }))
  })

  it('arma la ruta del dashboard', async () => {
    await obtenerDashboard()
    expect(apiRequest).toHaveBeenCalledWith(expect.objectContaining({ url: '/api/v1/bff/dashboard' }))
  })
})
