export async function setAuth(page, enabled) {
  await page.addInitScript((value) => {
    localStorage.setItem('e2e-auth', value)
  }, enabled ? 'on' : 'off')
}

const servicios = [
  {
    id: 1,
    tipoServicio: 'Habitación',
    nombre: 'Suite Alba',
    descripcion: 'Vista al jardín, cama king y desayuno de autor.',
    precio: 250000,
    capacidad: 2,
    disponible: true,
  },
  {
    id: 2,
    tipoServicio: 'Bienestar',
    nombre: 'Spa Sereno',
    descripcion: 'Rituales de relajación y aguas termales privadas.',
    precio: 75000,
    capacidad: 1,
    disponible: true,
  },
]

const dashboard = {
  usuario: { datos: { id: 1, nombre: 'Emilio' } },
  reservas: {
    datos: [{
      id: 8,
      fechaReserva: '2026-11-10',
      fechaTermino: '2026-11-12',
      tipoReserva: 'Doble',
      cantidadPersonas: 2,
      valorFinal: 120000,
      estadoPago: 'CONFIRMADO',
    }],
  },
  servicios: { datos: servicios },
}

export async function mockApi(page) {
  await page.route('**/api/v1/**', async (route) => {
    const url = new URL(route.request().url())

    if (url.pathname.endsWith('/servicio/list')) {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(servicios) })
      return
    }

    if (url.pathname.endsWith('/bff/dashboard')) {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(dashboard) })
      return
    }

    await route.fulfill({ status: 404, contentType: 'application/json', body: '{}' })
  })
}
