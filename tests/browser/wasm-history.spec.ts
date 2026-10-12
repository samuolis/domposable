import { test, expect } from '@playwright/test';

// Canvas controls have no DOM selectors. The shared demo's reviewed 390x844
// layout provides fixed interaction points; assertions check the real history
// binding and saveable registry rather than screenshot pixels.
test.beforeEach(async ({ page, browserName }) => {
  test.skip(browserName !== 'chromium', 'Wasm startup is currently verified on Chromium.');
  await page.setViewportSize({ width: 390, height: 844 });
});

async function state(page: import('@playwright/test').Page) {
  return page.evaluate(() => JSON.parse(history.state));
}

async function ready(page: import('@playwright/test').Page) {
  await expect(page.locator('body')).toHaveAttribute('data-domposable-ready', 'true', { timeout: 60000 });
  await expect.poll(async () => (await state(page)).entries.length).toBeGreaterThan(0);
}

async function frame(page: import('@playwright/test').Page) {
  await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => requestAnimationFrame(() => resolve()))));
}

test('Wasm application navigation follows browser back and forward with stable entry IDs', async ({ page }) => {
  await page.goto('/wasm/#/tasks');
  await ready(page);
  const initial = (await state(page)).entries[0].id;
  await page.mouse.click(195, 740); // Components navigation item.
  await expect(page).toHaveURL(/#\/catalog$/);
  const catalog = (await state(page)).entries.at(-1).id;
  await page.goBack();
  await expect(page).toHaveURL(/#\/tasks$/);
  await expect.poll(async () => (await state(page)).entries.at(-1).id).toBe(initial);
  await page.goForward();
  await expect(page).toHaveURL(/#\/catalog$/);
  await expect.poll(async () => (await state(page)).entries.at(-1).id).toBe(catalog);
  await page.reload();
  await ready(page);
  expect((await state(page)).entries.at(-1).id).toBe(catalog);
});

test('Wasm task notes survive application back, browser forward and reload', async ({ page }) => {
  await page.goto('/wasm/#/tasks');
  await ready(page);
  await page.mouse.click(190, 600); // Open the first task.
  await expect(page).toHaveURL(/#\/tasks\/welcome$/);
  const detail = (await state(page)).entries.at(-1).id;
  await frame(page);
  await page.mouse.click(150, 360); // Task notes.
  await frame(page);
  await page.keyboard.type('Wasm draft retained', { delay: 20 });
  await frame(page);
  await page.mouse.click(100, 175); // Application Back to tasks.
  await expect(page).toHaveURL(/#\/tasks$/);
  await page.goForward();
  await expect(page).toHaveURL(/#\/tasks\/welcome$/);
  await expect.poll(async () => (await state(page)).entries.at(-1).id).toBe(detail);
  await frame(page);
  await page.reload();
  await ready(page);
  const restored = (await state(page)).entries.at(-1);
  expect(restored.id).toBe(detail);
  expect(JSON.stringify(restored.saved)).toContain('Wasm draft retained');
});

test('Wasm ignores history state owned by another router', async ({ page }) => {
  await page.addInitScript(() => history.replaceState({ unrelatedRouter: true }, ''));
  await page.goto('/wasm/#/catalog');
  await ready(page);
  expect((await state(page)).entries.at(-1).route).toBe('/catalog');
});
