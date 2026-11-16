import { expect, test } from '@playwright/test'
import { setAuth } from '../helpers'

test.beforeEach(async ({ page }) => {
  await setAuth(page, false)
})

test('inicio muestra la marca y el hero', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByRole('heading', { name: /Bienvenido a Grand Hotel Group/ })).toBeVisible()
  await expect(page.getByRole('link', { name: 'Grand Hotel Group, inicio' })).toBeVisible()
})

test('login muestra el acceso Microsoft', async ({ page }) => {
  await page.goto('/login')
  await expect(page.getByRole('heading', { name: 'Iniciar sesión' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Iniciar sesión' })).toBeVisible()
})

test('una ruta protegida redirige a login sin sesión', async ({ page }) => {
  await page.goto('/servicios')
  await expect(page).toHaveURL(/\/login$/)
  await expect(page.getByRole('heading', { name: 'Iniciar sesión' })).toBeVisible()
})

test('navbar público no muestra panel admin', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByText('Iniciar sesión')).toBeVisible()
  await expect(page.getByText('Panel admin')).toHaveCount(0)
})

test('@visual inicio desktop', async ({ page }) => {
  await page.goto('/')
  await expect(page).toHaveScreenshot('inicio-1440.png')
})

test('@visual login desktop', async ({ page }) => {
  await page.goto('/login')
  await expect(page).toHaveScreenshot('login-1440.png')
})

test('@visual inicio móvil sin overflow', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto('/')
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)
  expect(overflow).toBe(false)
  await expect(page).toHaveScreenshot('inicio-390.png')
})
