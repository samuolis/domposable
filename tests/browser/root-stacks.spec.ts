import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import { mkdirSync } from 'node:fs';

test('independent roots restore drafts and identity, dispose sessions and reload portable snapshots', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/#/roots');
  await page.getByRole('button', { name: 'Open Inbox message', exact: true }).click();
  const note = page.getByRole('textbox', { name: 'Inbox message draft', exact: true });
  await note.fill('Inbox draft');
  const id = await page.getByText(/^Entry:/).textContent();
  const session = await page.getByText(/^Session:/).textContent();
  await page.getByRole('tab', { name: 'Archive', exact: true }).click();
  await page.getByRole('button', { name: 'Open Archive message', exact: true }).click();
  await page.getByRole('textbox', { name: 'Archive message draft', exact: true }).fill('Archive draft');
  await page.getByRole('tab', { name: 'Inbox', exact: true }).click();
  await expect(note).toHaveValue('Inbox draft');
  await expect(page.getByText(/^Entry:/)).toHaveText(id!);
  await expect(page.getByText(/^Session:/)).not.toHaveText(session!);
  await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => requestAnimationFrame(() => resolve()))));
  await page.reload();
  await expect(note).toHaveValue('Inbox draft');
  await expect(page.getByText(/^Entry:/)).toHaveText(id!);
  await page.getByRole('tab', { name: 'Archive', exact: true }).click();
  await expect(page.getByRole('textbox', { name: 'Archive message draft', exact: true })).toHaveValue('Archive draft');
  await page.getByRole('button', { name: 'Root back', exact: true }).click();
  await expect(page.getByRole('button', { name: 'Open Archive message', exact: true })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Root back', exact: true })).toHaveCount(0);
});

test('responsive root scenes preserve drafts when resizing', async ({ page, browserName }) => {
  await page.setViewportSize({ width: 1200, height: 900 });
  await page.goto('/#/roots');
  await page.getByRole('button', { name: 'Open Inbox message', exact: true }).click();
  const note = page.getByRole('textbox', { name: 'Inbox message draft', exact: true });
  await note.fill('Resize this draft');
  await expect(page.getByRole('heading', { name: 'Inbox home', exact: true })).toBeVisible();
  const id = await page.getByText(/^Entry:/).last().textContent();
  await page.setViewportSize({ width: 390, height: 844 });
  await expect(page.getByRole('heading', { name: 'Inbox home', exact: true })).toHaveCount(0);
  await expect(note).toHaveValue('Resize this draft');
  await expect(page.getByText(/^Entry:/)).toHaveText(id!);
  await page.setViewportSize({ width: 1200, height: 900 });
  await expect(page.getByRole('heading', { name: 'Inbox home', exact: true })).toBeVisible();
  await expect(note).toHaveValue('Resize this draft');
  expect((await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa']).analyze()).violations).toEqual([]);
  mkdirSync('build/screenshots/root-recipes', { recursive: true });
  await page.screenshot({ path: `build/screenshots/root-recipes/${browserName}.png` });
});
