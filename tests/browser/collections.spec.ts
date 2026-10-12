import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import { mkdirSync, writeFileSync } from 'node:fs';

const windowSelector = '[data-domposable-window="column"]';

test.beforeEach(async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/#/collections');
  await expect(page.locator(windowSelector)).toBeVisible();
});

test('ten thousand rows have bounded DOM and correct list positions', async ({ page }) => {
  const viewport = page.locator(windowSelector);
  await expect(viewport.locator('li')).toHaveCount(7);
  await viewport.evaluate(element => { element.scrollTop = 9500 * 92; });
  await expect(page.getByRole('textbox', { name: 'Row 9501 note', exact: true })).toBeVisible();
  const rows = viewport.locator('li');
  expect(await rows.count()).toBeLessThanOrEqual(12);
  await expect(rows.first()).toHaveAttribute('aria-posinset', '9498');
  await expect(rows.first()).toHaveAttribute('aria-setsize', '10000');
  expect((await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa']).analyze()).violations).toEqual([]);
});

test('focused rows remain mounted and saveable notes follow keys through scrolling and reorder', async ({ page }) => {
  const viewport = page.locator(windowSelector);
  const note = page.getByRole('textbox', { name: 'Row 1 note', exact: true });
  await note.fill('Persistent row draft');
  await viewport.evaluate(element => { element.scrollTop = 9500 * 92; });
  await expect(note).toBeFocused();
  expect(await viewport.locator('li').count()).toBeLessThanOrEqual(13);
  await page.getByRole('button', { name: 'Reverse rows', exact: true }).focus();
  await expect(note).toHaveCount(0);
  await viewport.evaluate(element => { element.scrollTop = 0; });
  await expect(note).toHaveValue('Persistent row draft');
  await page.getByRole('button', { name: 'Reverse rows', exact: true }).click();
  await viewport.evaluate(element => { element.scrollTop = element.scrollHeight; });
  await expect(note).toHaveValue('Persistent row draft');
  await page.getByRole('combobox', { name: 'Row count', exact: true }).selectOption('5');
  await expect(viewport.locator('li')).toHaveCount(5);
  await expect(note).toHaveValue('Persistent row draft');
});

test('removed keys discard saved row state', async ({ page }) => {
  const viewport = page.locator(windowSelector);
  await page.getByRole('button', { name: 'Reverse rows', exact: true }).click();
  const removed = page.getByRole('textbox', { name: 'Row 10000 note', exact: true });
  await removed.fill('Remove this row');
  await page.getByRole('combobox', { name: 'Row count', exact: true }).selectOption('5');
  await expect(removed).toHaveCount(0);
  await page.getByRole('combobox', { name: 'Row count', exact: true }).selectOption('10000');
  await expect(removed).toHaveValue('');
});

test('keyboard traversal reaches rows beyond the initially mounted window', async ({ page }) => {
  await page.getByRole('textbox', { name: 'Row 1 note', exact: true }).focus();
  for (let row = 2; row <= 20; row++) {
    await page.keyboard.press('Tab');
    // Let the browser apply focus scrolling and the next layout before the next key.
    await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => requestAnimationFrame(() => resolve()))));
    await expect(page.getByRole('textbox', { name: `Row ${row} note`, exact: true })).toBeFocused();
  }
});

test('collection benchmark records timings and gates bounded DOM rather than runner speed', async ({ page, browserName }) => {
  const timings: object[] = [];
  await page.getByRole('combobox', { name: 'Row count', exact: true }).selectOption('1000');
  for (const windowed of [false, true]) {
    const started = await page.evaluate(() => performance.now());
    await page.getByRole('switch', { name: 'Windowed list', exact: true }).setChecked(windowed);
    const list = windowed ? page.locator(windowSelector) : page.getByTestId('collection-lab-list');
    await expect(list.locator('li')).toHaveCount(windowed ? 7 : 1000);
    timings.push({ windowed, count: 1000, mountedRows: await list.locator('li').count(),
      durationMs: await page.evaluate(start => performance.now() - start, started) });
  }
  await page.getByRole('combobox', { name: 'Row count', exact: true }).selectOption('10000');
  await expect(page.locator(windowSelector).locator('li')).toHaveCount(7);
  mkdirSync('build/benchmarks', { recursive: true });
  writeFileSync(`build/benchmarks/collections-${browserName}.json`, JSON.stringify({ browserName, timings }, null, 2));
  await page.screenshot({ path: `build/benchmarks/collections-${browserName}.png` });
});
