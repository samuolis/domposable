import { test, expect, Page } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import { mkdirSync, writeFileSync } from 'node:fs';

const modes = [
  ['Measured column', 'measured-column'],
  ['Horizontal row', 'row'],
  ['Responsive grid', 'grid'],
  ['Semantic table', 'table'],
] as const;

async function select(page: Page, mode: string, kind: string) {
  await page.getByRole('combobox', { name: 'Collection layout', exact: true }).selectOption(mode);
  const viewport = page.locator(`[data-domposable-window="${kind}"]`);
  await expect(viewport).toBeVisible();
  await expect(page.getByRole('textbox', { name: 'Item 1 draft', exact: true })).toBeVisible();
  return viewport;
}

test.beforeEach(async ({ page }) => {
  await page.setViewportSize({ width: 1024, height: 900 });
  await page.goto('/#/collection-recipes');
  await expect(page.getByRole('heading', { name: 'Collection recipes' })).toBeVisible();
});

for (const [mode, kind] of modes) {
  test(`${mode}: bounded DOM, focused draft and stable keys across reorder and removal`, async ({ page, browserName }) => {
    const viewport = await select(page, mode, kind);
    const mounted = () => viewport.locator(kind === 'table' ? 'tbody tr[data-window-key]' : 'li');
    expect(await mounted().count()).toBeLessThanOrEqual(kind === 'grid' ? 48 : 24);
    const draft = page.getByRole('textbox', { name: 'Item 1 draft', exact: true });
    await draft.fill('Keep this keyed draft');
    await viewport.evaluate((element, horizontal) => {
      if (horizontal) element.scrollLeft = 9000 * 248;
      else element.scrollTop = element.scrollHeight * 0.9;
    }, kind === 'row');
    await expect(draft).toBeFocused();
    expect(await mounted().count()).toBeLessThanOrEqual(kind === 'grid' ? 49 : 25);
    await page.getByRole('button', { name: 'Reverse recipe items' }).focus();
    await expect(draft).toHaveCount(0);
    await viewport.evaluate(element => { element.scrollTop = 0; element.scrollLeft = 0; });
    await expect(draft).toHaveValue('Keep this keyed draft');
    await page.getByRole('button', { name: 'Reverse recipe items' }).click();
    await viewport.evaluate(element => { element.scrollTop = element.scrollHeight; element.scrollLeft = element.scrollWidth; });
    await expect(draft).toHaveValue('Keep this keyed draft');
    await page.getByRole('combobox', { name: 'Recipe item count' }).selectOption('5');
    await expect(mounted()).toHaveCount(5);
    await expect(draft).toHaveValue('Keep this keyed draft');
    await page.getByRole('combobox', { name: 'Recipe item count' }).selectOption('10000');
    await viewport.evaluate(element => { element.scrollTop = 0; element.scrollLeft = 0; });
    const removed = page.getByRole('textbox', { name: 'Item 10000 draft', exact: true });
    await removed.fill('Discard removed key');
    await page.getByRole('combobox', { name: 'Recipe item count' }).selectOption('5');
    await expect(removed).toHaveCount(0);
    await page.getByRole('combobox', { name: 'Recipe item count' }).selectOption('10000');
    await expect(removed).toHaveValue('');
    mkdirSync('build/benchmarks', { recursive: true });
    writeFileSync(`build/benchmarks/${kind}-${browserName}.json`, JSON.stringify({ count: 10000, mounted: await mounted().count() }));
  });
}

test('measured rows resize and keep the first visible item anchored', async ({ page }) => {
  const viewport = await select(page, 'Measured column', 'measured-column');
  await viewport.evaluate(element => { element.scrollTop = 4000; });
  await expect.poll(() => viewport.locator('li').evaluateAll(rows => {
    const container = rows[0]?.parentElement?.parentElement;
    if (!container) return false; // The keyed window can briefly be empty during recomposition.
    const top = container.getBoundingClientRect().top;
    return rows.some(row => row.getBoundingClientRect().bottom > top && row.getBoundingClientRect().top < top + 360);
  })).toBe(true);
  // Scroll dispatch, composition and ResizeObserver measurements precede the anchor sample.
  await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => requestAnimationFrame(() => requestAnimationFrame(() => resolve())))));
  const before = await viewport.locator('li').evaluateAll(rows => {
    const viewportTop = rows[0].parentElement!.parentElement!.getBoundingClientRect().top;
    const row = rows.find(row => row.getBoundingClientRect().bottom > viewportTop)!;
    return { key: row.getAttribute('data-window-key'), top: row.getBoundingClientRect().top - viewportTop };
  });
  await page.getByRole('switch', { name: 'Expanded item descriptions' }).check();
  const anchor = viewport.locator(`[data-window-key="${before.key}"]`);
  await expect(anchor).toBeAttached();
  await expect.poll(async () => {
    const position = await anchor.evaluate(row => row.getBoundingClientRect().top - row.parentElement!.parentElement!.getBoundingClientRect().top);
    return Math.abs(position - before.top);
  }).toBeLessThan(2);
  await page.setViewportSize({ width: 390, height: 844 });
  expect(await viewport.locator('li').count()).toBeLessThan(24);
});

test('responsive grid changes columns without losing drafts', async ({ page }) => {
  const viewport = await select(page, 'Responsive grid', 'grid');
  await expect(viewport).toHaveAttribute('data-window-columns', '4');
  const draft = page.getByRole('textbox', { name: 'Item 1 draft', exact: true });
  await draft.fill('Resize me');
  await page.setViewportSize({ width: 390, height: 844 });
  await expect(viewport).toHaveAttribute('data-window-columns', '2');
  await expect(draft).toHaveValue('Resize me');
  const widths = await viewport.locator('li').evaluateAll(rows => rows.map(row => row.getBoundingClientRect().width));
  expect(widths.every(width => width > 100 && width < 200)).toBe(true);
});

test('horizontal rows use logical offsets in RTL and keyboard focus advances beyond the first window', async ({ page }) => {
  await page.evaluate(() => { document.documentElement.dir = 'rtl'; });
  const viewport = await select(page, 'Horizontal row', 'row');
  await page.getByRole('textbox', { name: 'Item 1 draft', exact: true }).focus();
  for (let item = 2; item <= 15; item++) {
    await page.keyboard.press('Tab');
    await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => requestAnimationFrame(() => resolve()))));
    await expect(page.getByRole('textbox', { name: `Item ${item} draft`, exact: true })).toBeFocused();
  }
  const offset = await viewport.evaluate(element => element.scrollLeft);
  expect(offset).toBeLessThan(0);
  expect(await viewport.locator('li').count()).toBeLessThan(16);
});

test('large table retains caption, headers, row indices and clean HTML accessibility', async ({ page, browserName }) => {
  const viewport = await select(page, 'Semantic table', 'table');
  const table = page.getByRole('table', { name: 'Inventory drafts', exact: true });
  await expect(table).toHaveAttribute('aria-rowcount', '10001');
  await expect(table).toHaveAttribute('aria-colcount', '2');
  await expect(table.getByRole('columnheader')).toHaveCount(2);
  await expect(table.getByRole('columnheader', { name: 'Item', exact: true })).toHaveAttribute('scope', 'col');
  await viewport.evaluate(element => { element.scrollTop = element.scrollHeight; });
  await expect(page.getByRole('textbox', { name: 'Item 10000 draft', exact: true })).toBeVisible();
  await expect(viewport.locator('tr[data-window-key="item-9999"]')).toHaveAttribute('aria-rowindex', '10001');
  expect((await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa']).analyze()).violations).toEqual([]);
  if (browserName === 'chromium') {
    await viewport.evaluate(element => { element.scrollTop = 0; });
    mkdirSync('build/screenshots/dom', { recursive: true });
    await page.screenshot({ path: 'build/screenshots/dom/collection-table.png' });
    await select(page, 'Horizontal row', 'row');
    await page.screenshot({ path: 'build/screenshots/dom/collection-row.png' });
    await select(page, 'Responsive grid', 'grid');
    await page.screenshot({ path: 'build/screenshots/dom/collection-grid.png' });
  }
});
