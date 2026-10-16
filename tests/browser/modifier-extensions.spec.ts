import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

test('app-owned decoration preserves ordered padding, state and pointer interaction', async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.goto('/#/extensions');
  const frame = page.locator('[data-app-frame]');
  const paint = frame.locator('[data-app-frame-paint]');
  await expect(frame).toHaveCount(1);
  await expect(paint).toHaveAttribute('aria-hidden', 'true');
  await expect(paint).toHaveCSS('pointer-events', 'none');
  await expect(paint).toHaveCSS('border-top-color', 'rgb(64, 89, 173)');
  await expect(frame.locator('..')).toHaveCSS('padding-top', '8px');
  await expect(frame.locator(':scope > [data-domposable-modifier="Padding"]')).toHaveCSS('padding-top', '12px');
  await expect(frame.locator('[data-app-content-marker]')).toHaveCount(1);
  await expect(page.getByTestId('app-counter-label')).toHaveCount(1);
  const draft = page.getByRole('textbox', { name: 'Extension draft', exact: true });
  await draft.fill('A retained app draft');
  await page.getByRole('button', { name: 'Increment custom counter', exact: true }).click();
  await expect(page.getByTestId('app-counter-label')).toHaveText('Custom counter: 1');
  await page.getByRole('button', { name: 'Change frame color', exact: true }).click();
  await expect(paint).toHaveCSS('border-top-color', 'rgb(148, 103, 73)');
  await expect(draft).toHaveValue('A retained app draft');
  await expect(page.getByTestId('app-counter-label')).toHaveText('Custom counter: 1');
  const graphics = page.getByTestId('modifier-graphics');
  await expect(graphics.locator('xpath=ancestor::*[@data-domposable-modifier="Scale"]')).toHaveCSS('transform', 'matrix(0.9, 0, 0, 0.9, 0, 0)');
  await expect(graphics.locator('xpath=ancestor::*[@data-domposable-modifier="Rotation"]')).not.toHaveCSS('transform', 'none');
  await expect(graphics.locator('xpath=ancestor::*[@data-domposable-modifier="Shadow"]')).not.toHaveCSS('box-shadow', 'none');
  expect((await new AxeBuilder({ page }).analyze()).violations).toEqual([]);
  expect(errors).toEqual([]);
});

test('Wasm app-owned modifier keeps interactive content and semantics', async ({ page, browserName }) => {
  test.skip(browserName !== 'chromium', 'Wasm assistive interaction is verified in Chromium.');
  await page.goto('/wasm/#/extensions');
  const increment = page.getByRole('button', { name: 'Increment custom counter', exact: true });
  await expect(increment).toHaveCount(1, { timeout: 60000 });
  await increment.evaluate((element: HTMLElement) => element.click());
  await expect(page.getByText('Custom counter: 1', { exact: true })).toHaveCount(1);
  await page.getByRole('button', { name: 'Change frame color', exact: true }).evaluate((element: HTMLElement) => element.click());
  await expect(page.getByText('Custom counter: 1', { exact: true })).toHaveCount(1);
});
