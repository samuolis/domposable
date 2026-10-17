import { test as base, expect } from '@playwright/test';

const test = base.extend<{ runtimeErrors: void }>({
  runtimeErrors: [async ({ page }, use) => {
    const errors: string[] = [];
    page.on('pageerror', error => errors.push(error.stack || error.message));
    await use();
    expect(errors, 'The shared catalog must recompose without runtime errors').toEqual([]);
  }, { auto: true }],
});

async function openInputs(page: import('@playwright/test').Page) {
  await page.goto('/#/material/inputs');
}

test('calendar is a keyboard-operable grid and preserves a selected date', async ({ page }) => {
  await openInputs(page);
  const picker = page.getByRole('group', { name: 'Date picker' });
  const grid = picker.getByRole('grid');
  await expect(grid).toBeVisible();
  const selectedDay = grid.locator('.d-calendar-day[aria-pressed="true"]');
  await expect(selectedDay).toHaveCount(1);
  const target = grid.locator('.d-calendar-day:enabled:not([aria-pressed="true"])').first();
  await target.focus();
  await page.keyboard.press('ArrowRight');
  await expect(grid.locator('.d-calendar-day:focus')).toHaveCount(1);
  await page.keyboard.press('Enter');
  await expect(grid.locator('.d-calendar-day[aria-pressed="true"]')).toHaveCount(1);
});

test('time picker modes preserve and update controlled hour and minute values', async ({ page }) => {
  await openInputs(page);
  const picker = page.getByRole('group', { name: 'Time picker' });
  await picker.getByRole('button', { name: 'Hour 11', exact: true }).click();
  await picker.getByRole('button', { name: 'Minute 45', exact: true }).click();
  await expect(page.getByTestId('material-selected-time')).toContainText('11:45');
  const switchMode = page.getByRole('button', { name: 'Switch time entry', exact: true });
  await switchMode.click();
  const typedHour = picker.getByRole('textbox', { name: 'Hour', exact: true });
  const typedMinute = picker.getByRole('textbox', { name: 'Minute', exact: true });
  await expect(typedHour).toHaveValue('11');
  await expect(typedMinute).toHaveValue('45');
  await typedHour.fill('12');
  await expect(page.getByTestId('material-selected-time')).toContainText('12:45');
  await typedMinute.fill('15');
  await expect(page.getByTestId('material-selected-time')).toContainText('12:15');
  await switchMode.click();
  await expect(picker.getByRole('textbox')).toHaveCount(0);
  await expect(page.getByTestId('material-selected-time')).toContainText('12:15');
  await picker.getByRole('button', { name: 'Select hours', exact: true }).click();
  const hour = picker.getByRole('button', { name: 'Hour 12', exact: true });
  await expect(hour).toHaveAttribute('aria-pressed', 'true');
  await hour.focus();
  await page.keyboard.press('ArrowDown');
  await expect(page.getByTestId('material-selected-time')).toContainText('13:15');
});

test('autocomplete chooses with the keyboard without replacing its query separately', async ({ page }) => {
  await openInputs(page);
  const input = page.getByRole('combobox', { name: 'Favorite color' });
  await input.fill('Bl');
  await expect(input).toHaveAttribute('aria-expanded', 'true');
  // Focus can expand the previous results before the edited query recomposes.
  const suggestions = page.locator(`#${await input.getAttribute('aria-controls')}`);
  await expect(suggestions.getByRole('option')).toHaveText(['Blue']);
  await page.keyboard.press('ArrowDown');
  await page.keyboard.press('Enter');
  await expect(input).toHaveValue('Blue');
  await expect(input).toHaveAttribute('aria-expanded', 'false');
});

test('checkbox label keeps its click from activating an outer clickable row', async ({ page }) => {
  await page.goto('/#/material/selection');
  const checkbox = page.getByRole('checkbox', { name: 'Receive updates', exact: true });
  await checkbox.evaluate(input => {
    const outer = document.createElement('div');
    outer.dataset.testid = 'click-owner';
    outer.dataset.clickCount = '0';
    outer.addEventListener('click', () => {
      outer.dataset.clickCount = String(Number(outer.dataset.clickCount) + 1);
    });
    const label = input.closest('label')!;
    label.parentElement!.insertBefore(outer, label);
    outer.append(label);
  });
  await checkbox.click();
  await expect(checkbox).toBeChecked();
  await expect(page.getByTestId('click-owner')).toHaveAttribute('data-click-count', '0');
});

test('date range start cannot be cleared while leaving an orphan end date', async ({ page }) => {
  await openInputs(page);
  const range = page.getByRole('group', { name: 'Date range' });
  await range.getByRole('button', { name: 'Enter dates', exact: true }).click();
  const start = range.getByLabel('Start date');
  const end = range.getByLabel('End date');
  await expect(start).toHaveValue('1970-01-01');
  await expect(end).toHaveValue('1970-01-02');
  await start.fill('');
  await expect(start).toHaveValue('');
  await expect(end).toHaveValue('');
  await expect(end).toBeDisabled();
});

test('scroll field wheel movement updates the controlled value', async ({ page }) => {
  await openInputs(page);
  await expect(page.getByRole('heading', { name: 'Material catalog', exact: true })).toBeInViewport();
  const wheel = page.getByRole('spinbutton', { name: 'Hour wheel' });
  await expect(wheel).toHaveAttribute('aria-valuenow', '10');
  const viewport = wheel.locator('.d-scroll-field-viewport');
  await viewport.hover();
  await page.mouse.wheel(0, 88);
  await expect(wheel).not.toHaveAttribute('aria-valuenow', '10');
  await expect(page.getByTestId('material-selected-time')).not.toContainText('10:');
});

test('selected chip stays selected after its controlled click callback', async ({ page }) => {
  await page.goto('/#/material/selection');
  const chip = page.getByRole('button', { name: 'Configured filter', exact: true });
  await chip.click();
  await expect(chip).toHaveAttribute('aria-pressed', 'true');
  await expect(chip).toHaveCSS('font-weight', '600');
  await expect(chip).toHaveCSS('border-top-width', '3px');
});

test('snackbar host presents queued message actions and completes the request', async ({ page }) => {
  await page.goto('/#/material/feedback');
  await page.getByRole('button', { name: 'Queue message', exact: true }).click();
  const message = page.getByRole('status').filter({ hasText: 'Document saved' });
  await expect(message).toBeVisible();
  await message.locator('..').getByRole('button', { name: 'Undo', exact: true }).click();
  await expect(page.getByTestId('material-snackbar-result')).toHaveText('ActionPerformed');
});

test('search exposes an expanded results dialog and keeps the chosen query controlled', async ({ page }) => {
  await openInputs(page);
  const search = page.getByRole('combobox', { name: 'Search products', exact: true });
  await search.fill('Green');
  const results = page.getByRole('dialog', { name: 'Search results', exact: true });
  await expect(results).toBeVisible();
  await expect(search).toHaveAttribute('aria-expanded', 'true');
  await results.getByRole('button', { name: 'Green', exact: true }).click();
  await expect(search).toHaveValue('Green');
  await expect(search).toHaveAttribute('aria-expanded', 'false');
  await expect(results).not.toBeVisible();
});

test('controls embed in app-owned labels and mixed selection commits true', async ({ page }) => {
  await page.goto('/#/material/selection');
  for (const [role, name] of [
    ['checkbox', 'App-owned document selection'],
    ['radio', 'App-owned account selection'],
  ] as const) {
    const control = page.getByRole(role, { name, exact: true });
    await control.scrollIntoViewIfNeeded();
    await control.evaluate(input => {
      const host = input.closest('.d-toggle')!;
      if (host.tagName === 'LABEL') throw new Error('Hidden-label controls must support an app-owned label');
      const label = document.createElement('label');
      host.parentElement!.insertBefore(label, host);
      label.append(host);
    });
    if (role === 'checkbox') {
      await expect(control).toHaveJSProperty('indeterminate', true);
      // Browsers support either underlying checked value while painting mixed state.
      await control.evaluate(input => { (input as HTMLInputElement).checked = true; });
    }
    await control.click();
    await expect(control).toBeChecked();
    await expect(control.locator('xpath=ancestor::label')).toHaveCount(1);
    if (role === 'checkbox') await expect(control).toHaveJSProperty('indeterminate', false);
  }
});


test('time editing keeps partial text and rejects out-of-range values', async ({ page }) => {
  await openInputs(page);
  await page.getByRole('button', { name: 'Switch time entry', exact: true }).click();
  const picker = page.getByRole('group', { name: 'Time picker' });
  const hour = picker.getByRole('textbox', { name: 'Hour', exact: true });
  const minute = picker.getByRole('textbox', { name: 'Minute', exact: true });
  await hour.fill('');
  await hour.pressSequentially('23');
  await expect(hour).toHaveValue('23');
  await expect(page.getByTestId('material-selected-time')).toContainText('23:30');
  await minute.fill('59');
  await expect(page.getByTestId('material-selected-time')).toContainText('23:59');
  await hour.fill('99');
  await expect(hour).toHaveAttribute('aria-invalid', 'true');
  await expect(page.getByTestId('material-selected-time')).toContainText('23:59');
  await minute.focus();
  await expect(hour).toHaveValue('23');
});

test('24-hour clock places midnight and noon on distinct concentric rings', async ({ page }) => {
  await openInputs(page);
  const picker = page.getByRole('group', { name: 'Time picker' });
  const midnight = (await picker.getByRole('button', { name: 'Hour 00', exact: true }).boundingBox())!;
  const noon = (await picker.getByRole('button', { name: 'Hour 12', exact: true }).boundingBox())!;
  const one = (await picker.getByRole('button', { name: 'Hour 01', exact: true }).boundingBox())!;
  const thirteen = (await picker.getByRole('button', { name: 'Hour 13', exact: true }).boundingBox())!;
  expect(Math.abs(midnight.x - noon.x)).toBeLessThan(1);
  expect(midnight.y - noon.y).toBeGreaterThan(30);
  expect(one.x).toBeGreaterThan(thirteen.x);
  expect(one.y).toBeLessThan(thirteen.y);
});

test('calendar uses locale week order and circular Material day targets', async ({ page }) => {
  await openInputs(page);
  const picker = page.getByRole('group', { name: 'Date picker' });
  await expect(picker.locator('.d-date-picker-title')).toHaveText('Select date');
  const firstHeader = picker.getByRole('columnheader').first();
  await expect(firstHeader).toHaveAttribute('aria-label', 'Sunday');
  const selected = picker.locator('.d-calendar-day[aria-pressed="true"]');
  await expect(selected).toHaveCSS('width', '40px');
  await expect(selected).toHaveCSS('height', '40px');
  await expect(selected).toHaveCSS('border-radius', '50%');
});
