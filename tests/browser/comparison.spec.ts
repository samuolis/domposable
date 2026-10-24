import { test, expect } from '@playwright/test';

test('comparison switches the HTML feature shell at the selected width', async ({ page }) => {
  await page.goto('/compare/');
  const html = page.frameLocator('iframe[title="Domposable semantic HTML preview"]');
  await expect(html.getByRole('heading', { name: 'Task workspace', exact: true })).toBeVisible();
  await page.getByLabel('Preview width', { exact: true }).selectOption('390');
  await page.getByLabel('Feature', { exact: true }).selectOption('shop');
  await expect(html.getByRole('heading', { name: 'FIELDWORK', exact: true })).toBeVisible();
  expect(await html.locator('body').evaluate(() => window.innerWidth)).toBe(390);
  await page.getByLabel('Feature', { exact: true }).selectOption('catalog');
  await expect(html.getByRole('heading', { name: 'Component catalog', exact: true })).toBeVisible();
});

test('comparison reloads the Wasm feature rather than only changing its hash', async ({ page, browserName }) => {
  test.skip(browserName !== 'chromium', 'Wasm startup and visual references currently use Chromium.');
  test.setTimeout(90000);
  await page.goto('/compare/');
  const wasm = page.frameLocator('iframe[title="Domposable Wasm preview"]');
  await expect(wasm.locator('body')).toHaveAttribute('data-domposable-ready', 'true', { timeout: 60000 });
  await page.getByLabel('Feature', { exact: true }).selectOption('shop');
  await expect(wasm.locator('body')).toHaveAttribute('data-shop-artwork-ready', 'true', { timeout: 60000 });
  await page.getByLabel('Feature', { exact: true }).selectOption('catalog');
  await expect(wasm.locator('body')).not.toHaveAttribute('data-shop-artwork-ready', 'true');
  await expect(wasm.locator('body')).toHaveAttribute('data-domposable-ready', 'true', { timeout: 60000 });
  await expect(wasm.locator('canvas').first()).toBeVisible();
});


test('component comparison initializes equal theme and state routes in both frames', async ({ page }) => {
  await page.goto('/compare/');
  await page.getByLabel('Preview width', { exact: true }).selectOption('640');
  await page.getByLabel('Feature', { exact: true }).selectOption('compare/states');
  const html = page.frameLocator('iframe[title="Domposable semantic HTML preview"]');
  await expect(html.getByRole('heading', { name: 'states · light', exact: true })).toBeVisible();
  await page.getByLabel('Component theme', { exact: true }).selectOption('brand-dark');
  await expect(html.getByRole('heading', { name: 'states · brand-dark', exact: true })).toBeVisible();
  await expect(html.getByRole('checkbox', { name: 'Disabled selected checkbox', exact: true })).toBeDisabled();
  await expect(html.getByRole('checkbox', { name: 'Disabled selected checkbox', exact: true })).toBeChecked();
  const wasm = page.locator('iframe[title="Domposable Wasm preview"]');
  await expect(wasm).toHaveAttribute('src', /#\/material\/compare\/brand-dark\/states$/);
  expect(await html.locator('body').evaluate(() => window.innerWidth)).toBe(640);
});
