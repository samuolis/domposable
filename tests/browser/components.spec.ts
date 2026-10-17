import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

test('shared selection controls, actions and feedback use real HTML', async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.goto('/#/components');
  await expect(page.getByRole('heading', { name: 'More shared components' })).toBeVisible();
  const delivery = page.getByRole('combobox', { name: 'Delivery', exact: true });
  await expect(delivery).toHaveJSProperty('tagName', 'SELECT');
  await expect(delivery.getByRole('option', { name: 'Same day (unavailable)', exact: true })).toBeDisabled();
  await delivery.selectOption('express');
  await page.getByRole('radio', { name: 'Office', exact: true }).check();
  await expect(page.getByText('Deliver EXPRESS to Office', { exact: true })).toBeVisible();
  await page.getByRole('button', { name: 'Save preferences', exact: true }).click();
  await expect(page.getByRole('status')).toHaveText('Preferences saved');
  await page.getByRole('button', { name: 'Reset', exact: true }).click();
  await expect(delivery).toHaveValue('standard');
  await expect(page.getByRole('radio', { name: 'Home', exact: true })).toBeChecked();
  await page.getByRole('button', { name: 'Dismiss', exact: true }).click();
  await expect(page.getByRole('status')).toHaveCount(0);
  expect((await new AxeBuilder({ page }).analyze()).violations).toEqual([]);
  expect(errors).toEqual([]);
});

test('range endpoints stay ordered and keyboard interaction updates the feature', async ({ page }) => {
  await page.goto('/#/components');
  const minimum = page.getByRole('slider', { name: 'Minimum price', exact: true });
  const maximum = page.getByRole('slider', { name: 'Maximum price', exact: true });
  const startBox = (await minimum.boundingBox())!;
  const endBox = (await maximum.boundingBox())!;
  expect(startBox.y).toBe(endBox.y);
  expect(startBox.height).toBe(48);
  // Both halves of the shared track remain pointer targets for their endpoint.
  await minimum.click({ position: { x: startBox.width * 0.1, y: startBox.height / 2 } });
  await expect(minimum).toHaveValue('10');
  await maximum.click({ position: { x: endBox.width * 0.9, y: endBox.height / 2 } });
  await expect(maximum).toHaveValue('90');
  await minimum.press('ArrowRight');
  await maximum.press('ArrowLeft');
  await minimum.focus();
  await minimum.press('ArrowRight');
  await expect(page.getByText('Price range: 30–80', { exact: true })).toBeVisible();
  // Home must clamp to the other endpoint instead of allowing the thumbs to cross.
  await maximum.focus();
  await maximum.press('Home');
  await expect(maximum).toHaveValue('30');
  await expect(maximum).toHaveAttribute('aria-valuemin', '30');
  await expect(minimum).toHaveAttribute('aria-valuemax', '30');
  // Repeated attempts to cross must stay clamped even when the app state is unchanged.
  await maximum.press('Home');
  await expect(maximum).toHaveValue('30');
  await minimum.focus();
  await minimum.press('End');
  await expect(minimum).toHaveValue('30');
  await minimum.press('End');
  await expect(minimum).toHaveValue('30');
});

test('tooltip describes its control, opens with keyboard focus and dismisses with Escape', async ({ page }) => {
  await page.goto('/#/components');
  const control = page.getByRole('button', { name: 'About preferences', exact: true });
  const tooltip = page.getByRole('tooltip');
  await control.focus();
  await expect(tooltip).toBeVisible();
  await expect(control).toHaveAttribute('aria-describedby', await tooltip.getAttribute('id') as string);
  await control.press('Escape');
  await expect(tooltip).toBeHidden();
  await expect(control).toBeFocused();
  await page.getByRole('combobox', { name: 'Delivery', exact: true }).focus();
  await control.hover();
  await expect(tooltip).toBeVisible();
});


test('logical padding follows right-to-left layout', async ({ page }) => {
  await page.addInitScript(() => document.addEventListener('DOMContentLoaded', () => { document.documentElement.dir = 'rtl'; }));
  await page.goto('/#/components');
  const control = page.getByRole('button', { name: 'About preferences', exact: true });
  const padding = await control.evaluate(button => {
    const wrapper = button.closest('.d-tooltip-anchor')!.parentElement!;
    const style = getComputedStyle(wrapper);
    return { start: style.paddingRight, end: style.paddingLeft };
  });
  expect(padding).toEqual({ start: '4px', end: '12px' });
});

test('Escape dismisses a focused tooltip before its containing modal', async ({ page }) => {
  await page.goto('/#/components');
  const open = page.getByRole('button', { name: 'About preferences', exact: true });
  await open.focus();
  await open.press('Enter');
  const dialog = page.getByRole('dialog', { name: 'About preferences', exact: true });
  const details = dialog.getByRole('button', { name: 'Show demo details', exact: true });
  await details.focus();
  await expect(dialog.getByRole('tooltip')).toBeVisible();
  await details.press('Escape');
  await expect(dialog.getByRole('tooltip')).toBeHidden();
  await expect(dialog).toBeVisible();
  await expect(details).toBeFocused();
  await details.press('Escape');
  await expect(dialog).toBeHidden();
  await expect(open).toBeFocused();
});
