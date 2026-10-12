import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

test('document and data controls use browser semantics and shared callbacks', async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/#/product-components');
  await expect(page.getByRole('heading', { name: 'Documents and data lab', exact: true })).toBeVisible();
  if (test.info().project.name === 'chromium') {
    await page.screenshot({ path: 'build/screenshots/dom/product-components.png' });
  }
  const form = page.getByRole('form', { name: 'Delivery preferences', exact: true });
  await expect(form).toHaveJSProperty('tagName', 'FORM');
  const email = page.getByRole('textbox', { name: /^Contact email/ });
  await expect(email).toHaveAttribute('type', 'email');
  await expect(email).toHaveAttribute('placeholder', 'you@example.com');
  await expect(page.getByRole('radio', { name: 'Same day', exact: true })).toBeDisabled();
  const express = page.getByRole('radio', { name: 'Express', exact: true });
  await page.locator('label').filter({ has: express }).click();
  await expect(express).toBeChecked();
  await email.fill('hello@example.com');
  await email.press('Enter');
  await expect(page.getByTestId('product-form-status')).toHaveText('Saved preferences for hello@example.com');
  const disclosure = page.getByRole('button', { name: 'Shipping details', exact: true });
  await expect(disclosure).toHaveAttribute('aria-expanded', 'false');
  await disclosure.click();
  await expect(disclosure).toHaveAttribute('aria-expanded', 'true');
  await expect(page.getByRole('link', { name: 'Shipping policy', exact: true })).toHaveAttribute('href', 'https://example.com/shipping');
  const chart = page.locator('[data-domposable-line-chart] svg');
  await expect(chart).toHaveJSProperty('namespaceURI', 'http://www.w3.org/2000/svg');
  await chart.focus();
  await chart.press('ArrowRight');
  await expect(page.getByTestId('product-chart-status')).toHaveText('Selected day 1');
  await chart.press('ArrowRight');
  await expect(page.getByTestId('product-chart-status')).toHaveText('Selected day 2');
  await chart.press('Escape');
  await expect(page.getByTestId('product-chart-status')).toHaveText('No selected day');
  const table = page.getByTestId('product-orders').getByRole('table');
  await expect(table).toHaveJSProperty('tagName', 'TABLE');
  await expect(table.getByRole('columnheader', { name: 'Order', exact: true })).toBeVisible();
  const row = table.getByRole('row').filter({ has: page.getByRole('rowheader', { name: 'A-101', exact: true }) });
  await row.focus();
  await row.press('Enter');
  await expect(page.getByTestId('product-row-status')).toHaveText('Opened order A-101');
  const shortNote = page.getByRole('textbox', { name: 'One-row note', exact: true });
  const longNote = page.getByRole('textbox', { name: 'Longer note', exact: true });
  await expect(shortNote).toHaveAttribute('rows', '1');
  await expect(longNote).toHaveAttribute('rows', '3');
  await shortNote.fill('First line\nSecond line');
  await expect(shortNote).toHaveValue('First line\nSecond line');
  const bounded = await longNote.evaluate(element => {
    const field = element as HTMLTextAreaElement;
    const lineHeight = parseFloat(getComputedStyle(field).lineHeight);
    field.style.height = '500px';
    const maximum = field.getBoundingClientRect().height;
    field.style.height = '1px';
    const minimum = field.getBoundingClientRect().height;
    return { lineHeight, maximum, minimum };
  });
  expect(bounded.maximum).toBeCloseTo(4 * bounded.lineHeight + 26, 1);
  expect(bounded.minimum).toBeCloseTo(3 * bounded.lineHeight + 26, 1);
  expect((await new AxeBuilder({ page }).analyze()).violations).toEqual([]);
  expect(errors).toEqual([]);
});

test('Wasm document controls execute the same feature callbacks', async ({ page, browserName }) => {
  test.skip(browserName !== 'chromium', 'Wasm interactions are verified in Chromium.');
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/wasm/#/product-components');
  const email = page.getByRole('textbox', { name: /^Contact email/ });
  await expect(email).toHaveCount(1, { timeout: 60000 });
  await page.screenshot({ path: 'build/screenshots/wasm/product-components.png' });
  await email.focus();
  await expect(page.locator('.compose-backing-field')).toBeFocused();
  await page.keyboard.insertText('hello@example.com');
  const submit = page.getByRole('button', { name: 'Save preferences', exact: true });
  await expect(submit).toBeEnabled();
  await submit.evaluate((element: HTMLElement) => element.click());
  await expect(page.getByText('Saved preferences for hello@example.com', { exact: true })).toHaveCount(1);
  await page.getByRole('button', { name: 'Shipping details', exact: true })
    .evaluate((element: HTMLElement) => element.click());
  await expect(page.getByText('Free shipping', { exact: false })).toHaveCount(1);
});
