import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

test('HTML feature supports input, adaptive panes and browser history', async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.setViewportSize({ width: 1440, height: 900 });
  await page.goto('/#/tasks');
  await expect(page.getByRole('heading', { name: 'Task workspace' })).toBeVisible();
  await page.getByTestId('task-welcome').click();
  await page.getByTestId('task-notes').fill('Preserve this draft');
  await expect(page.locator('[data-entry-id]')).toHaveCount(2);
  const detailId = await page.locator('[data-entry-id]').last().getAttribute('data-entry-id');
  await page.setViewportSize({ width: 390, height: 844 });
  await expect(page.locator('[data-entry-id]')).toHaveCount(1);
  await expect(page.getByTestId('task-notes')).toHaveValue('Preserve this draft');
  await expect(page.locator('[data-entry-id]')).toHaveAttribute('data-entry-id', detailId!);
  await page.getByRole('button', { name: 'Components', exact: false }).click();
  await expect(page.getByRole('heading', { name: 'Component catalog' })).toBeVisible();
  await page.goBack();
  await expect(page.getByTestId('task-notes')).toHaveValue('Preserve this draft');
  await page.goForward();
  await expect(page.getByRole('heading', { name: 'Component catalog' })).toBeVisible();
  await expect(page.locator('canvas')).toHaveCount(0);
  expect(errors).toEqual([]);
});

test('semantic controls, focus restoration and replaceable renderer', async ({ page }, testInfo) => {
  await page.goto('/#/catalog');
  await page.getByTestId('catalog-input').fill('Architecture independent');
  await page.getByRole('checkbox', { name: 'Checkbox', exact: true }).check();
  await expect(page.getByRole('switch', { name: 'Switch', exact: true })).toBeChecked();
  await page.getByRole('tab', { name: 'First' }).focus();
  await page.keyboard.press('ArrowRight');
  await expect(page.getByRole('tab', { name: 'Second' })).toBeFocused();
  await expect(page.getByRole('tab', { name: 'Second' })).toHaveAttribute('aria-selected', 'true');
  const trigger = page.getByRole('button', { name: 'Open dialog' });
  await trigger.click();
  await expect(page.getByRole('dialog', { name: 'Example dialog' })).toBeVisible();
  await page.keyboard.press('Escape');
  await expect(page.getByRole('dialog')).toHaveCount(0);
  await expect(trigger).toBeFocused();
  await page.getByRole('switch', { name: 'Custom buttons' }).check();
  await expect(page.getByRole('button', { name: 'Filled', exact: true })).toHaveAttribute('data-custom-renderer', 'true');
  await expect(page.getByTestId('catalog-input')).toHaveValue('Architecture independent');
  await expect(page.getByRole('button', { name: 'Disabled', exact: true })).toBeDisabled();
  const results = await new AxeBuilder({ page }).analyze();
  expect(results.violations.map(v => ({ id: v.id, targets: v.nodes.map(n => n.target) }))).toEqual([]);
  await page.screenshot({ path: testInfo.outputPath('component-catalog.png'), fullPage: true });
});

test('history restores numeric saveables and reload beneath a project base path', async ({ page }) => {
  await page.goto('/#/catalog');
  await page.getByRole('slider', { name: 'Progress' }).fill('0.75');
  await page.getByTestId('catalog-input').fill('Reload state');
  await page.getByRole('button', { name: 'Tasks', exact: false }).click();
  await expect(page).toHaveURL(/#\/tasks$/);
  await page.goBack();
  await expect(page.getByRole('slider', { name: 'Progress' })).toHaveValue('0.75');
  await expect(page.getByTestId('catalog-input')).toHaveValue('Reload state');
  await page.reload();
  await expect(page.getByTestId('catalog-input')).toHaveValue('Reload state');
  await expect(page.getByRole('slider', { name: 'Progress' })).toHaveValue('0.75');
});

test('application back traverses history and forward restores the disposed model data', async ({ page }) => {
  await page.goto('/#/tasks');
  await page.getByTestId('task-welcome').click();
  await expect(page).toHaveURL(/#\/tasks\/welcome$/);
  await page.getByTestId('task-notes').fill('Forward restores a fresh model');
  await page.getByRole('button', { name: 'Back to tasks' }).click();
  await expect(page).toHaveURL(/#\/tasks$/);
  await page.goForward();
  await expect(page.getByTestId('task-notes')).toHaveValue('Forward restores a fresh model');
});
