import { test, expect, Page } from '@playwright/test';
import { mkdir, writeFile } from 'node:fs/promises';
import { createHash } from 'node:crypto';

test.skip(({ browserName }) => browserName !== 'chromium', 'Visual baselines use pinned Chromium on Ubuntu; behavior/axe checks cover all three engines.');

async function stableCapture(page: Page, platform: string, name: string) {
  // The playground's diamond/checkmark glyphs use a canvas fallback font loaded after the
  // first frame. document.fonts.ready and a previously reached networkidle do not cover it.
  if (platform === 'wasm' && !name.startsWith('shop')) {
    await page.waitForFunction(() => performance.getEntriesByType('resource')
      .some(resource => resource.name.includes('/notosanssymbols2/')));
    await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => requestAnimationFrame(() => resolve()))));
  }
  await page.evaluate(() => document.fonts.ready);
  let previous = '';
  let capture: Buffer;
  await expect.poll(async () => {
    capture = await page.screenshot({ animations: 'disabled', caret: 'hide', scale: 'css' });
    const hash = createHash('sha256').update(capture).digest('hex');
    const stable = hash === previous;
    previous = hash;
    return stable;
  }, { timeout: 20000, intervals: [250, 500, 1000] }).toBe(true);
  await mkdir(`build/screenshots/${platform}`, { recursive: true });
  await writeFile(`build/screenshots/${platform}/${name}.png`, capture!);
}

for (const [name, width, height, route] of [
  ['tasks-phone', 390, 844, 'tasks'], ['tasks-tablet', 768, 900, 'tasks'],
  ['tasks-wide', 1440, 900, 'tasks'], ['catalog-phone', 390, 844, 'catalog'],
  ['catalog-wide', 1440, 900, 'catalog'], ['extended-components', 1024, 1100, 'components'], ['recipes-wide', 1440, 900, 'recipes'], ['collection-measured', 1024, 900, 'collection-recipes'],
] as const) {
  test(`DOM visual ${name}`, async ({ page }) => {
    await page.setViewportSize({ width, height });
    await page.goto(`/#/${route}`);
    await expect(page.locator('main h1, main h2').first()).toBeVisible();
    await stableCapture(page, 'dom', name);
  });
}
test('DOM visual detail-wide', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 900 });
  await page.goto('/#/tasks');
  await page.getByTestId('task-welcome').click();
  await expect(page.getByTestId('task-notes')).toBeVisible();
  await stableCapture(page, 'dom', 'detail-wide');
});
test('DOM visual dialog-phone', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/#/catalog');
  await page.getByRole('button', { name: 'Open dialog' }).click();
  await expect(page.getByRole('dialog')).toBeVisible();
  await stableCapture(page, 'dom', 'dialog-phone');
});

for (const [name, width, height, route] of [
  ['tasks-phone', 390, 844, 'tasks'], ['tasks-wide', 1440, 900, 'tasks'],
  ['catalog-wide', 1440, 900, 'catalog'], ['extended-components', 1024, 1100, 'components'], ['recipes-wide', 1440, 900, 'recipes'], ['collection-measured', 1024, 900, 'collection-recipes'],
  ['navigation-policies', 1024, 1000, 'navigation-policies'],
] as const) {
  test(`Wasm visual ${name}`, async ({ page }) => {
    const errors: string[] = [];
    page.on('pageerror', error => errors.push(error.message));
    await page.setViewportSize({ width, height });
    await page.goto(`/wasm/#/${route}`);
    await expect(page.locator('canvas').first()).toBeVisible({ timeout: 60000 });
    await expect(page.locator('body')).toHaveAttribute('data-domposable-ready', 'true', { timeout: 60000 });
    await expect(page.locator('body')).toHaveAttribute('data-domposable-initial-route', `/${route}`);
    await stableCapture(page, 'wasm', name);
    expect(errors).toEqual([]);
  });
}

for (const [name, width, height, route] of [
  ['shop-phone', 390, 844, ''], ['shop-wide', 1440, 900, ''],
  ['shop-collection-wide', 1440, 900, 'collection/'],
] as const) {
  test(`DOM visual ${name}`, async ({ page }) => {
    await page.setViewportSize({ width, height });
    await page.goto(`/shop/${route}`);
    await expect(page.getByRole('heading', { name: 'FIELDWORK' })).toBeVisible();
    await expect(page.locator('main img').first()).toHaveAttribute('data-testid', /-loaded$/);
    await stableCapture(page, 'dom', name);
  });
}
test('DOM visual shop-cart-phone', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/shop/collection/');
  await page.getByRole('button', { name: 'Add Canvas weekender to bag', exact: true }).click();
  await page.getByRole('link', { name: 'Bag (1)', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Your bag' })).toBeVisible();
  await expect(page.locator('main img').first()).toHaveAttribute('data-testid', /-loaded$/);
  await stableCapture(page, 'dom', 'shop-cart-phone');
});
for (const [name, width, height] of [['shop-phone',390,844],['shop-wide',1440,900]] as const) {
  test(`Wasm visual ${name}`, async ({ page }) => {
    const errors: string[] = [];
    page.on('pageerror', error => errors.push(error.message));
    await page.setViewportSize({ width, height });
    await page.goto('/wasm/#/shop');
    await expect(page.locator('body')).toHaveAttribute('data-domposable-ready','true',{timeout:60000});
    // Resource loading must complete before recording the canvas.
    await expect(page.locator('body')).toHaveAttribute('data-shop-artwork-ready','true',{timeout:20000});
    await stableCapture(page,'wasm',name);
    expect(errors).toEqual([]);
  });
}
