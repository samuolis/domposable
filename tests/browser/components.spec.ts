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
  await minimum.focus();
  await minimum.press('ArrowRight');
  await expect(page.getByText('Price range: 30–80', { exact: true })).toBeVisible();
  // Home must clamp to the other endpoint instead of allowing the thumbs to cross.
  await maximum.focus();
  await maximum.press('Home');
  await expect(maximum).toHaveValue('30');
  await expect(maximum).toHaveAttribute('aria-valuemin', '30');
  await expect(minimum).toHaveAttribute('aria-valuemax', '30');
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
