import { createHash } from 'node:crypto';
import { test, expect } from '@playwright/test';

test('HTML production entry is fingerprinted and nested routes reload', async ({ page, request }) => {
  const release = await (await request.get('/domposable/release.json')).json();
  const entry = release.web.html.entry;
  expect(entry).toMatch(/^demo\.[a-f0-9]{64}\.js$/);
  const bundle = await request.get(`/domposable/demo/${entry}`);
  expect(bundle.ok()).toBeTruthy();
  expect(createHash('sha256').update(await bundle.body()).digest('hex')).toBe(entry.slice(5, -3));
  expect((await request.get('/domposable/demo/demo.js')).status()).toBe(404);
  await page.goto('/domposable/shop/products/canvas-weekender/');
  await expect(page.getByRole('heading', { name: 'Canvas weekender', exact: true })).toBeVisible();
  const script = page.locator(`script[src="${entry}"]`);
  await expect(script).toHaveCount(1);
  await page.reload();
  await expect(page.getByRole('heading', { name: 'Canvas weekender', exact: true })).toBeVisible();
  await page.goto('/domposable/routes/tasks/welcome/');
  await expect(page.getByText('Try one feature on three platforms', { exact: true }).first()).toBeVisible();
});

test('Wasm production entry and binaries load through hashed asset URLs', async ({ page, request, browserName }) => {
  test.skip(browserName !== 'chromium', 'Wasm production runtime currently uses Chromium.');
  test.setTimeout(90000);
  const release = await (await request.get('/domposable/release.json')).json();
  const entry = release.web.wasm.entry;
  const manifest = await (await request.get('/domposable/wasm/web-assets.json')).json();
  expect(manifest.sha256).toBe(release.web.wasm.sha256);
  expect(entry).toMatch(/^demo\.[a-f0-9]{64}\.js$/);
  const bundle = await request.get(`/domposable/wasm/${entry}`);
  expect(createHash('sha256').update(await bundle.body()).digest('hex')).toBe(entry.slice(5, -3));
  const binaries = Object.keys(manifest.files).filter(name => name.endsWith('.wasm'));
  expect(binaries.length).toBeGreaterThan(0);
  for (const name of binaries) {
    expect(name).toMatch(/^[a-f0-9]+\.wasm$/);
    const binary = await request.get(`/domposable/wasm/${name}`);
    expect(binary.ok()).toBeTruthy();
    expect(createHash('sha256').update(await binary.body()).digest('hex')).toBe(manifest.files[name]);
  }
  await page.goto('/domposable/wasm/#/shop');
  await expect(page.locator('body')).toHaveAttribute('data-domposable-ready', 'true', { timeout: 60000 });
  await expect(page.locator('body')).toHaveAttribute('data-shop-artwork-ready', 'true', { timeout: 60000 });
  await expect(page.locator('canvas').first()).toBeVisible();
});
