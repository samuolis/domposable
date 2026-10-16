import { test, expect } from '@playwright/test';

test.beforeEach(async ({ page }) => {
  await page.setViewportSize({ width: 900, height: 700 });
  await page.goto('/#/caller-history');
  await expect(page.getByRole('heading', { name: 'Caller-owned browser history' })).toBeVisible();
  await expect.poll(async () => page.evaluate(() => history.state)).toBe('0:home');
});

test('caller-owned history flushes rapid pushes and restores browser back, forward and refresh', async ({ page }) => {
  const initialLength = await page.evaluate(() => history.length);

  await page.getByRole('button', { name: 'Push two routes' }).click();
  await expect(page).toHaveURL(/#\/caller-history\/third\?source=fixture#summary$/);
  await expect(page.getByTestId('caller-history-page')).toHaveText('Current page: third');
  await expect(page.getByTestId('caller-history-position')).toHaveText('Physical position: 2');
  await expect.poll(async () => page.evaluate(() => history.length)).toBe(initialLength + 2);
  expect(await page.evaluate(() => history.state)).toBe('2:third');

  await page.goBack();
  await expect(page).toHaveURL(/#\/caller-history\/second\?source=fixture#notes$/);
  await expect(page.getByTestId('caller-history-page')).toHaveText('Current page: second');
  await expect(page.getByTestId('caller-history-position')).toHaveText('Physical position: 1');

  await page.goForward();
  await expect(page).toHaveURL(/#\/caller-history\/third\?source=fixture#summary$/);
  await expect(page.getByTestId('caller-history-page')).toHaveText('Current page: third');
  await page.reload();
  await expect(page.getByTestId('caller-history-page')).toHaveText('Current page: third');
  expect(await page.evaluate(() => history.state)).toBe('2:third');

  await page.goBack();
  await page.goBack();
  await expect(page).toHaveURL(/#\/caller-history$/);
  await expect(page.getByTestId('caller-history-page')).toHaveText('Current page: home');
  await expect(page.getByTestId('caller-history-position')).toHaveText('Physical position: 0');
  expect(await page.evaluate(() => history.state)).toBe('0:home');

  await page.getByRole('button', { name: 'Push two routes' }).click();
  await expect(page).toHaveURL(/#\/caller-history\/third\?source=fixture#summary$/);
  await expect(page.getByTestId('caller-history-position')).toHaveText('Physical position: 2');
  await expect.poll(async () => page.evaluate(() => history.length)).toBe(initialLength + 2);
  expect(await page.evaluate(() => history.state)).toBe('2:third');
});

test('caller-owned history falls back to the URL without rewriting invalid state and rejects external URLs', async ({ page }) => {
  await page.evaluate(() => {
    history.replaceState('tampered', '', '#/caller-history/fallback?source=url#arrival');
    window.dispatchEvent(new PopStateEvent('popstate'));
  });
  await expect(page.getByTestId('caller-history-page')).toHaveText('Current page: fallback');
  expect(await page.evaluate(() => history.state)).toBe('tampered');
  await expect(page).toHaveURL(/#\/caller-history\/fallback\?source=url#arrival$/);

  await page.getByRole('button', { name: 'Reject external URL' }).click();
  await expect(page.getByTestId('caller-history-url-rejected')).toHaveText('Rejected external URL: true');
  expect(await page.evaluate(() => history.state)).toBe('tampered');
  await expect(page).toHaveURL(/#\/caller-history\/fallback\?source=url#arrival$/);
});
