import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

test('Kobweb exports shared content without requiring client JavaScript', async ({ browser, baseURL }) => {
  const context = await browser.newContext({ javaScriptEnabled: false });
  const page = await context.newPage();
  const response = await page.goto(`${baseURL}/contacts/alex`);
  expect(response?.status()).toBe(200);
  await expect(page.getByRole('heading', { name: 'Alex', exact: true })).toBeVisible();
  await expect(page.getByRole('link', { name: 'All contacts' })).toHaveAttribute('href', '/contacts');
  await context.close();
});

test('Kobweb owns dynamic URLs, browser history and deep-link reloads', async ({ page, browserName }) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.goto('/contacts');
  await page.getByRole('textbox', { name: 'Search contacts' }).fill('Alex');
  await page.getByRole('button', { name: 'Open Alex' }).click();
  await expect(page).toHaveURL(/\/contacts\/alex$/);
  await expect(page.getByRole('heading', { name: 'Alex', exact: true })).toBeVisible();
  expect((await page.reload())?.status()).toBe(200);
  await expect(page.getByRole('heading', { name: 'Alex', exact: true })).toBeVisible();
  await page.goBack();
  await expect(page).toHaveURL(/\/contacts$/);
  await expect(page.getByRole('heading', { name: 'Contacts', exact: true })).toBeVisible();
  const scan = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa']).analyze();
  expect(scan.violations).toEqual([]);
  expect(errors).toEqual([]);
  await page.screenshot({ path: `build/integration-captures/kobweb-contacts-${browserName}.png` });
});

test('static hosting does not conceal missing dynamic exports', async ({ request }) => {
  expect((await request.get('/contacts/not-exported')).status()).toBe(404);
});
