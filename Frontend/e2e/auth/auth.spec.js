import { expect, test } from '@playwright/test'
import { mockApi, setAuth } from '../helpers'

test.beforeEach(async ({ page }) => {
  await setAuth(page, true)
  await mockApi(page)
})

test('servicios autenticado muestra el catálogo', async ({ page }) => {
  await page.goto('/servicios')
  await expect(page.getByRole('heading', { name: 'Servicios' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Suite Alba' })).toBeVisible()
})

test('reservas autenticado muestra la reserva y su estado', async ({ page }) => {
  await page.goto('/mis_reservas')
  await expect(page.getByRole('heading', { name: 'Mis Reservas' })).toBeVisible()
  await expect(page.getByText('Reserva #8')).toBeVisible()
  await expect(page.getByText('CONFIRMADO')).toBeVisible()
})

test('mi cuenta muestra perfil y resumen', async ({ page }) => {
  await page.goto('/mi_cuenta')
  await expect(page.getByRole('heading', { name: 'Mi cuenta' })).toBeVisible()
  await expect(page.getByText('admin@e2e.test')).toBeVisible()
  await expect(page.getByText('2 servicios')).toBeVisible()
})

test('panel admin muestra el catálogo administrable', async ({ page }) => {
  await page.goto('/PanelAdministradores')
  await expect(page.getByRole('heading', { name: 'Panel admin' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Registrar servicio' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Suite Alba' })).toBeVisible()
})

test('@visual servicios desktop', async ({ page }) => {
  await page.goto('/servicios')
  await expect(page).toHaveScreenshot('servicios-1440.png')
})

test('@visual reservas desktop', async ({ page }) => {
  await page.goto('/mis_reservas')
  await expect(page).toHaveScreenshot('reservas-1440.png')
})

test('@visual cuenta desktop', async ({ page }) => {
  await page.goto('/mi_cuenta')
  await expect(page).toHaveScreenshot('cuenta-1440.png')
})

test('@visual panel admin desktop', async ({ page }) => {
  await page.goto('/PanelAdministradores')
  await expect(page).toHaveScreenshot('admin-1440.png')
})

test('mensajeria muestra Rabbit y Kafka en vivo', async ({ page }) => {
  await page.goto('/mensajeria')
  await expect(page.getByRole('heading', { name: 'Mensajería' })).toBeVisible()
  await expect(page.getByText('analytics-group')).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Notificaciones' })).toBeVisible()
})

test('@visual mensajeria desktop', async ({ page }) => {
  await page.goto('/mensajeria')
  await expect(page).toHaveScreenshot('mensajeria-1440.png')
})
