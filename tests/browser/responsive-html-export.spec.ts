import { test, expect } from '@playwright/test';
import { mkdir, mkdtemp, readFile, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { exportPages } from '../../tools/page-export/index.mjs';

const fixturePath = '/fixtures/responsive-export/';

test('responsive HTML layouts resize in-browser and survive real export without JavaScript', async ({ browser, page, baseURL, browserName }) => {
  test.skip(browserName !== 'chromium', 'The exporter owns a Chromium capture.');
  test.setTimeout(60_000);
  await page.goto(fixturePath);
  await expect(page.getByRole('heading', { level: 1, name: 'Responsive export fixture' })).toBeVisible();

  for (const [width, expectedColumns] of [[390, 1], [768, 2], [1024, 3], [1280, 4]] as const) {
    await page.setViewportSize({ width, height: 1000 });
    await expect.poll(() => columnCount(page)).toBe(expectedColumns);
    const expectedHeight = width >= 1200 ? 220 : width >= 840 ? 180 : width >= 600 ? 140 : 100;
    await expect(page.locator('#responsive-height')).toHaveCSS('height', `${expectedHeight}px`);
  }

  await page.setViewportSize({ width: 390, height: 1000 });
  await expect.poll(() => splitDirection(page, 'split-short')).toBe('column');
  await expect.poll(() => splitDirection(page, 'split-long')).toBe('column');
  const phoneOrder = await splitBounds(page, 'split-short');
  expect(phoneOrder.text.y).toBeLessThan(phoneOrder.media.y);
  expect(phoneOrder.text.width).toBeCloseTo(phoneOrder.media.width, 0);
  const phoneMediaFirst = await splitBounds(page, 'split-long');
  expect(phoneMediaFirst.media.y).toBeLessThan(phoneMediaFirst.text.y);
  expect(phoneMediaFirst.media.width).toBeCloseTo(240, 0);

  for (const width of [768, 1280]) {
    await page.setViewportSize({ width, height: 1000 });
    await expect.poll(() => splitDirection(page, 'split-short')).toBe('row');
    await expect.poll(() => splitDirection(page, 'split-long')).toBe('row');
  }
  const desktopBounds = await splitBounds(page, 'split-long');
  expect(desktopBounds.media.width).toBeCloseTo(240, 0);
  expect(desktopBounds.media.x).toBeLessThan(desktopBounds.text.x);
  expect(desktopBounds.media.height).toBeCloseTo(520, 0);
  expect(desktopBounds.mediaContent.height).toBeCloseTo(400, 0);
  expect(desktopBounds.mediaHeight.height).toBeCloseTo(400, 0);
  expect(desktopBounds.art.height).toBeCloseTo(400, 0);
  expect(desktopBounds.art.y + desktopBounds.art.height / 2).toBeCloseTo(
    desktopBounds.media.y + desktopBounds.media.height / 2, 0);
  expect(desktopBounds.mediaHeight.y + desktopBounds.mediaHeight.height / 2).toBeCloseTo(
    desktopBounds.media.y + desktopBounds.media.height / 2, 0);
  expect(desktopBounds.mediaHeight.y).toBeGreaterThan(desktopBounds.media.y);
  const shortBounds = await splitBounds(page, 'split-short');
  expect(shortBounds.media.height).toBeCloseTo(200, 0);
  expect(shortBounds.mediaContent.height).toBeCloseTo(200, 0);
  expect(shortBounds.mediaHeight.height).toBeCloseTo(200, 0);
  expect(shortBounds.art.height).toBeCloseTo(200, 0);
  expect(shortBounds.text.height).toBeLessThan(shortBounds.media.height);
  const middleBounds = await splitBounds(page, 'split-middle');
  expect(middleBounds.media.height).toBeCloseTo(300, 0);
  expect(middleBounds.mediaContent.height).toBeCloseTo(300, 0);
  expect(middleBounds.mediaHeight.height).toBeCloseTo(300, 0);
  expect(middleBounds.art.height).toBeCloseTo(300, 0);
  expect(middleBounds.mediaHeight.y + middleBounds.mediaHeight.height / 2).toBeCloseTo(
    middleBounds.media.y + middleBounds.media.height / 2, 0);

  const output = await mkdtemp(join(tmpdir(), 'domposable-responsive-export-'));
  try {
    await exportPages({ browser, baseUrl: baseURL!, canonicalOrigin: 'https://example.test', outputDir: output,
      pages: [{ path: fixturePath }] });
    const html = await readFile(join(output, 'fixtures/responsive-export/index.html'), 'utf8');
    expect(html).toContain('@container');
    expect(html).toContain('data-d-html-responsive-split');

    const noScript = await browser.newContext({ javaScriptEnabled: false, viewport: { width: 390, height: 1000 } });
    const exportedPage = await noScript.newPage();
    await exportedPage.setContent(html);
    await expect(exportedPage.getByRole('heading', { level: 1, name: 'Responsive export fixture' })).toBeVisible();
    await expect.poll(() => columnCount(exportedPage)).toBe(1);
    await expect.poll(() => splitDirection(exportedPage, 'split-short')).toBe('column');
    await mkdir(join(process.cwd(), 'build/screenshots/dom'), { recursive: true });
    await exportedPage.screenshot({ path: join(process.cwd(), 'build/screenshots/dom/responsive-export-phone.png'), fullPage: true });

    await exportedPage.setViewportSize({ width: 1280, height: 1000 });
    await expect.poll(() => columnCount(exportedPage)).toBe(4);
    await expect.poll(() => splitDirection(exportedPage, 'split-short')).toBe('row');
    const exportedBounds = await splitBounds(exportedPage, 'split-long');
    expect(exportedBounds.media.width).toBeCloseTo(240, 0);
    expect(exportedBounds.media.height).toBeCloseTo(520, 0);
    expect(exportedBounds.mediaContent.height).toBeCloseTo(400, 0);
    expect(exportedBounds.mediaHeight.height).toBeCloseTo(400, 0);
    expect(exportedBounds.art.height).toBeCloseTo(400, 0);
    expect(exportedBounds.art.y + exportedBounds.art.height / 2).toBeCloseTo(
      exportedBounds.media.y + exportedBounds.media.height / 2, 0);
    expect(exportedBounds.mediaHeight.y + exportedBounds.mediaHeight.height / 2).toBeCloseTo(
      exportedBounds.media.y + exportedBounds.media.height / 2, 0);
    expect(await exportedPage.locator('#responsive-grid').evaluate(node => getComputedStyle(node).display)).toBe('grid');
    await exportedPage.screenshot({ path: join(process.cwd(), 'build/screenshots/dom/responsive-export-wide.png'), fullPage: true });
    await noScript.close();
  } finally {
    await rm(output, { recursive: true, force: true });
  }
});

async function columnCount(page: import('@playwright/test').Page): Promise<number> {
  return page.locator('[data-grid-item]').evaluateAll(elements =>
    new Set(elements.map(element => Math.round(element.getBoundingClientRect().x))).size);
}

async function splitDirection(page: import('@playwright/test').Page, id: string): Promise<string> {
  return page.locator(`#${id}`).evaluate(element => getComputedStyle(element).flexDirection);
}

async function splitBounds(page: import('@playwright/test').Page, id: string) {
  return page.locator(`#${id}`).evaluate(element => {
    const bounds = (selector: string) => {
      const rect = element.querySelector(selector)!.getBoundingClientRect();
      return { x: rect.x, y: rect.y, width: rect.width, height: rect.height };
    };
    return {
      media: bounds('[data-d-html-responsive-split-media]'),
      mediaContent: bounds('[data-d-html-responsive-split-media-content]'),
      mediaHeight: bounds(`#${element.id}-media-height`),
      art: bounds(`#${element.id}-media-art`),
      text: bounds(`#${element.id}-text`),
    };
  });
}
