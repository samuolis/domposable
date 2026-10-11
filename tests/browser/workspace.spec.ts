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
  await page.getByRole('link', { name: 'Components', exact: false }).click();
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
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.goto('/#/catalog');
  await page.getByRole('slider', { name: 'Progress' }).fill('0.75');
  await page.getByRole('tab', { name: 'Second', exact: true }).click();
  await page.getByTestId('catalog-input').fill('Reload state');
  await page.getByRole('link', { name: 'Tasks', exact: false }).click();
  await expect(page).toHaveURL(/#\/tasks$/);
  await page.goBack();
  await expect(page.getByRole('slider', { name: 'Progress' })).toHaveValue('0.75');
  await expect(page.getByRole('tab', { name: 'Second', exact: true })).toHaveAttribute('aria-selected', 'true');
  await expect(page.getByTestId('catalog-input')).toHaveValue('Reload state');
  await page.reload();
  await expect(page.getByTestId('catalog-input')).toHaveValue('Reload state');
  await expect(page.getByRole('slider', { name: 'Progress' })).toHaveValue('0.75');
  await expect(page.getByRole('tab', { name: 'Second', exact: true })).toHaveAttribute('aria-selected', 'true');
  expect(errors).toEqual([]);
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

test('landmarks, portable links and wrapping controls fit narrow layouts', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/#/catalog');
  await expect(page.getByRole('banner')).toBeVisible();
  await expect(page.getByRole('main')).toBeVisible();
  await expect(page.getByRole('contentinfo')).toBeVisible();
  await expect(page.getByRole('link', { name: 'Source' })).toHaveAttribute('href', 'https://github.com/samuolis/domposable');
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(390);
  const result = await new AxeBuilder({ page }).analyze();
  expect(result.violations.map(v => v.id)).toEqual([]);
});

test('Navigation 3 recipe preserves drafts across pane changes and pop', async ({ page }) => {
  await page.setViewportSize({ width: 1600, height: 1000 });
  await page.goto('/#/recipes');
  await page.getByTestId('recipe-open').click();
  await page.getByTestId('recipe-draft').fill('Shared draft');
  await page.getByRole('switch', { name: 'Allow three panes' }).check();
  await page.getByRole('button', { name: 'Show supporting pane' }).click();
  await expect(page.getByRole('heading', { name: 'Inbox', exact: true })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Context', exact: true })).toBeVisible();
  await page.setViewportSize({ width: 390, height: 844 });
  await expect(page.getByRole('heading', { name: 'Inbox', exact: true })).toHaveCount(0);
  await page.getByRole('button', { name: 'Recipe back' }).click();
  await expect(page.getByTestId('recipe-draft')).toHaveValue('Shared draft');
});

test('path URLs support deep links, history and reload below a hosting base', async ({ page }) => {
  await page.goto('/routes/tasks/welcome/');
  await expect(page.getByTestId('task-notes')).toBeVisible();
  await expect(page).toHaveURL(/\/routes\/tasks\/welcome\/$/);
  await page.getByTestId('task-notes').fill('Path URL draft');
  await page.reload();
  await expect(page.getByTestId('task-notes')).toHaveValue('Path URL draft');
  await page.getByRole('link', { name: 'Components', exact: false }).click();
  await expect(page).toHaveURL(/\/routes\/catalog\/$/);
  await page.goBack();
  await expect(page.getByTestId('task-notes')).toHaveValue('Path URL draft');
  await page.goForward();
  await expect(page.getByRole('heading', { name: 'Component catalog' })).toBeVisible();
});

test('dynamic path fallback and unknown routes preserve meaningful URLs', async ({ page }) => {
  await page.goto('/routes/tasks/missing-demo-task/');
  await expect(page.getByRole('alert')).toHaveText('This task is unavailable');
  await expect(page).toHaveURL(/\/routes\/tasks\/missing-demo-task\/$/);
  await page.goto('/routes/unknown-page/');
  await expect(page.getByRole('heading', { name: 'Page not found' })).toBeVisible();
  await expect(page).toHaveURL(/\/routes\/unknown-page\/$/);
  await page.getByRole('button', { name: 'Return to tasks' }).click();
  await expect(page).toHaveURL(/\/routes\/tasks\/$/);
  await expect(page.getByRole('link', { name: 'Components', exact: false })).toHaveAttribute('href', '/routes/catalog/');
});
