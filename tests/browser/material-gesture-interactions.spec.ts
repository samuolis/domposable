import { test as base, expect } from '@playwright/test';

const test = base.extend<{ runtimeErrors: void }>({
  runtimeErrors: [async ({ page }, use) => {
    const errors: string[] = [];
    page.on('pageerror', error => errors.push(error.stack || error.message));
    await use();
    expect(errors, 'The Material gesture examples must not throw runtime errors').toEqual([]);
  }, { auto: true }],
});

test('swipe dismissal exposes direction labels and keyboard actions', async ({ page }) => {
  await page.goto('/#/material/gestures');
  const swipe = page.getByTestId('material-swipe-document');
  await expect(swipe).toHaveAttribute(
    'aria-label',
    'Swipe to dismiss. ArrowRight: Archive document. ArrowLeft: Delete document.',
  );
  await expect(swipe).toHaveAttribute('aria-keyshortcuts', 'ArrowRight ArrowLeft');

  await swipe.focus();
  await page.keyboard.press('ArrowRight');
  await expect(page.getByRole('button', { name: 'Restore dismissed item' })).toBeVisible();
});

test('active swipe announces the direction-specific action label', async ({ page }) => {
  await page.goto('/#/material/gestures');
  const swipe = page.getByTestId('material-swipe-document');
  await swipe.scrollIntoViewIfNeeded();
  const bounds = await swipe.boundingBox();
  if (!bounds) throw new Error('Swipe item is not laid out');
  const x = bounds.x + bounds.width / 2;
  const y = bounds.y + bounds.height / 2;
  await page.mouse.move(x, y);
  await page.mouse.down();
  await page.mouse.move(x + 56, y, { steps: 4 });
  await expect(swipe.locator('[aria-live="polite"]')).toHaveText('Archive document');
  await page.mouse.up();
  await expect(page.getByRole('button', { name: 'Restore dismissed item' })).toBeVisible();
});

test('bottom sheet handle honors the swipe setting and expands by dragging when enabled', async ({ page }) => {
  await page.goto('/#/material/scaffold');
  const handle = page.getByRole('button', { name: 'Expand', exact: true });
  const gestures = page.getByRole('switch', { name: 'Sheet gestures', exact: true });
  await expect(handle).toHaveAttribute('aria-expanded', 'false');

  await gestures.uncheck();
  await handle.scrollIntoViewIfNeeded();
  let bounds = await handle.boundingBox();
  if (!bounds) throw new Error('Bottom sheet handle is not laid out');
  await page.mouse.move(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
  await page.mouse.down();
  await page.mouse.move(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2 - 80, { steps: 5 });
  await page.mouse.up();
  await expect(handle).toHaveAttribute('aria-expanded', 'false');

  await gestures.check();
  await handle.scrollIntoViewIfNeeded();
  bounds = await handle.boundingBox();
  if (!bounds) throw new Error('Bottom sheet handle is not laid out');
  await page.mouse.move(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
  await page.mouse.down();
  await page.mouse.move(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2 - 80, { steps: 5 });
  await page.mouse.up();
  await expect(page.getByRole('button', { name: 'Collapse', exact: true })).toHaveAttribute('aria-expanded', 'true');
});

test('drag handle exposes keyboard resize actions without claiming an absolute slider value', async ({ page }) => {
  await page.goto('/#/material/gestures');
  const handle = page.getByRole('button', { name: 'Resize panel', exact: true });
  await handle.focus();
  await page.keyboard.press('ArrowUp');
  await expect(page.getByText('Panel width: 201', { exact: true })).toBeVisible();
  await page.keyboard.press('Enter');
  await expect(page.getByText('Panel width: 202', { exact: true })).toBeVisible();
});
