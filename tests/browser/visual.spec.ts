import { test, expect, Page } from '@playwright/test';
import { stableCapture } from './stable-capture';

test.skip(({ browserName }) => browserName !== 'chromium', 'Visual baselines use pinned Chromium on Ubuntu; behavior/axe checks cover all three engines.');


const materialSections = [
  ['actions', 'Choose an action'],
  ['selection', 'Receive updates'],
  ['inputs', 'Outlined field'],
  ['surfaces', 'Local surface override'],
  ['navigation', 'Home'],
  ['feedback', 'No queued message'],
  ['gestures', 'Swipe or use the dismissal action'],
  ['scaffold', 'The app owns its state, injection and navigation.'],
] as const;

async function pauseMaterialClock(page: Page) {
  const fixedTime = new Date('2026-10-15T12:00:00Z');
  await page.clock.install({ time: fixedTime });
  // Pause ahead of the installation instant so a slow browser cannot make it a past timestamp.
  await page.clock.pauseAt(new Date(fixedTime.getTime() + 1000));
}

for (const [name, width, height, route] of [
  ['tasks-phone', 390, 844, 'tasks'], ['tasks-tablet', 768, 900, 'tasks'],
  ['tasks-wide', 1440, 900, 'tasks'], ['catalog-phone', 390, 844, 'catalog'],
  ['catalog-wide', 1440, 900, 'catalog'], ['extended-components', 1024, 1100, 'components'], ['recipes-wide', 1440, 900, 'recipes'], ['collection-measured', 1024, 900, 'collection-recipes'],
  ['composition-phone', 390, 844, 'composition'], ['composition-wide', 1440, 1000, 'composition'],
  ['extensions-phone', 390, 844, 'extensions'], ['extensions-wide', 1440, 900, 'extensions'],
] as const) {
  test(`DOM visual ${name}`, async ({ page }) => {
    await page.setViewportSize({ width, height });
    await page.goto(`/#/${route}`);
    await expect(page.locator('main h1, main h2').first()).toBeVisible();
    if (route === 'composition') await expect(page.getByTestId('shared-card-loaded')).toBeVisible();
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
  const progress = page.locator('.d-progress.d-progress-standard[role="progressbar"]');
  await expect.poll(() => progress.evaluate(element => element.getBoundingClientRect().height)).toBe(4);
  await stableCapture(page, 'dom', 'dialog-phone');
});

for (const [name, width, height, route] of [
  ['tasks-phone', 390, 844, 'tasks'], ['tasks-wide', 1440, 900, 'tasks'],
  ['catalog-wide', 1440, 900, 'catalog'], ['extended-components', 1024, 1100, 'components'], ['recipes-wide', 1440, 900, 'recipes'], ['collection-measured', 1024, 900, 'collection-recipes'],
  ['navigation-policies', 1024, 1000, 'navigation-policies'],
  ['composition-phone', 390, 844, 'composition'], ['composition-wide', 1440, 1000, 'composition'],
  ['extensions', 1440, 900, 'extensions'],
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
  await stableCapture(page, 'dom', 'shop-cart-phone', async () => {
    await expect(page.getByRole('heading', { name: 'Your bag' })).toBeVisible();
  });
});

for (const [name, expanded] of [
  ['long-sheet-partial-phone', false],
  ['long-sheet-expanded-phone', true],
] as const) {
  test(`DOM visual ${name}`, async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto('/#/material/compare/light/integration');
    await expect(page.getByTestId('shared-weighted-row-box')).toBeVisible();
    const box = await page.getByTestId('shared-weighted-row-box').boundingBox();
    const trailing = await page.getByTestId('shared-weighted-row-trailing').boundingBox();
    const comparison = await page.getByTestId('material-comparison').boundingBox();
    const paddingRight = await page.getByTestId('shared-weighted-row-box').evaluate(element => {
      const wrapper = element.parentElement;
      return wrapper ? Number.parseFloat(getComputedStyle(wrapper).paddingRight) : Number.NaN;
    });
    expect(box).not.toBeNull();
    expect(trailing).not.toBeNull();
    expect(comparison).not.toBeNull();
    const availableRight = comparison!.x + comparison!.width - paddingRight;
    expect(Math.abs(box!.x + box!.width - availableRight)).toBeLessThanOrEqual(3);
    expect(Math.abs(trailing!.x + trailing!.width - availableRight)).toBeLessThanOrEqual(3);

    await page.getByTestId('shared-long-sheet-open').click();
    const sheet = page.locator('dialog.d-sheet');
    await expect(sheet).toBeVisible();
    const sheetHeight = () => sheet.evaluate(element => element.getBoundingClientRect().height);
    const partialHeight = await sheetHeight();
    expect(partialHeight).toBeGreaterThan(0);
    expect(partialHeight).toBeLessThanOrEqual(844 * 0.5 + 4);
    if (expanded) {
      const handle = sheet.locator('.d-sheet-drag-handle');
      const bounds = await handle.boundingBox();
      expect(bounds).not.toBeNull();
      await page.mouse.move(bounds!.x + bounds!.width / 2, bounds!.y + bounds!.height / 2);
      await page.mouse.down();
      await page.mouse.move(bounds!.x + bounds!.width / 2, bounds!.y + bounds!.height / 2 - 180, { steps: 4 });
      await page.mouse.up();
      await expect(sheet).toHaveClass(/d-sheet-expanded/);
      await expect.poll(sheetHeight).toBeGreaterThan(partialHeight + 100);
      await expect.poll(sheetHeight).toBeGreaterThan(844 - 40);
    }
    await stableCapture(page, 'dom', name);
  });
}
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

test('DOM visual themed controls', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/#/catalog');
  await page.getByTestId('themed-action').scrollIntoViewIfNeeded();
  await stableCapture(page, 'dom', 'themed-controls');
});

for (const [section, content] of materialSections) {
  test(`DOM visual material-${section}`, async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 1000 });
    await page.goto(`/#/material/${section}`);
    await expect(page.getByRole('heading', { name: 'Material catalog' })).toBeVisible();
    await expect(page.getByRole('heading', { name: section[0].toUpperCase() + section.slice(1), exact: true })).toBeVisible();
    const sample = page.locator('main').getByText(content, { exact: true }).first();
    await sample.scrollIntoViewIfNeeded();
    await expect(sample).toBeVisible();
    await stableCapture(page, 'dom', `material-${section}`, () => pauseMaterialClock(page));
  });
}

for (const [section] of materialSections) {
  test(`Wasm visual material-${section}`, async ({ page }) => {
    const errors: string[] = [];
    page.on('pageerror', error => errors.push(error.message));
    await page.setViewportSize({ width: 1440, height: 1200 });
    await page.goto(`/wasm/#/material/${section}`);
    await expect(page.locator('canvas').first()).toBeVisible({ timeout: 60000 });
    await expect(page.locator('body')).toHaveAttribute('data-domposable-ready', 'true', { timeout: 60000 });
    await expect(page.locator('body')).toHaveAttribute('data-domposable-initial-route', `/material/${section}`);
    await stableCapture(page, 'wasm', `material-${section}`, () => pauseMaterialClock(page));
    expect(errors).toEqual([]);
  });
}
