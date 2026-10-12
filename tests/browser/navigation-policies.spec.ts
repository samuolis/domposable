import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import { mkdirSync } from 'node:fs';

test.beforeEach(async ({ page }) => {
  await page.setViewportSize({ width: 1024, height: 1000 });
  await page.goto('/#/navigation-policies');
  await expect(page.getByRole('heading', { name: 'Navigation policies' })).toBeVisible();
});

test('live model identity and draft survive independent root switches', async ({ page }) => {
  await page.getByRole('button', { name: 'Open Inbox live message' }).click();
  const identity = await page.getByTestId('live-Inbox-message').textContent();
  await page.getByRole('textbox', { name: 'Inbox message live draft', exact: true }).fill('Still alive');
  await page.getByRole('tab', { name: 'Archive', exact: true }).click();
  await page.getByRole('button', { name: 'Open Archive live message' }).click();
  await page.getByRole('textbox', { name: 'Archive message live draft', exact: true }).fill('Other root');
  await page.getByRole('tab', { name: 'Inbox', exact: true }).click();
  await expect(page.getByTestId('live-Inbox-message')).toHaveText(identity!);
  await expect(page.getByRole('textbox', { name: 'Inbox message live draft', exact: true })).toHaveValue('Still alive');
  await page.reload();
  // The model scope is live memory; only the root routes/entry IDs are saved across reload.
  await expect(page.getByRole('textbox', { name: 'Inbox message live draft', exact: true })).toHaveValue('');
});

test('preview cancellation preserves the stack and commit requests one guarded transition', async ({ page }) => {
  await page.getByRole('button', { name: 'Open Inbox live message' }).click();
  const slider = page.getByRole('slider', { name: 'Simulated back progress' });
  await slider.scrollIntoViewIfNeeded();
  await slider.focus();
  await expect(slider).toHaveValue('0');
  // Finish the viewport/focus-triggered Compose layout before applying the range key.
  await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => requestAnimationFrame(() => resolve()))));
  await slider.press('End');
  await expect(page.getByText('Preview progress: 100%', { exact: true })).toBeVisible();
  await page.getByRole('button', { name: 'Cancel preview', exact: true }).click();
  await expect(page.getByRole('textbox', { name: 'Inbox message live draft', exact: true })).toBeAttached();
  await expect(page.getByText('Preview progress: 0%', { exact: true })).toBeVisible();
  await slider.scrollIntoViewIfNeeded();
  await slider.focus();
  await expect(slider).toHaveValue('0');
  // Finish the viewport/focus-triggered Compose layout before applying the range key.
  await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => requestAnimationFrame(() => resolve()))));
  await slider.press('End');
  await page.getByRole('button', { name: 'Commit preview', exact: true }).click();
  await expect(page.getByRole('textbox', { name: 'Inbox message live draft', exact: true })).toHaveCount(0);
  await expect(page.getByRole('button', { name: 'Open Inbox live message', exact: true })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Commit preview', exact: true })).toBeDisabled();
});

test('route overlay isolates focus, cancels with Escape and leaves its underlying model alive', async ({ page, browserName }) => {
  await page.getByRole('button', { name: 'Open Inbox live message' }).click();
  const identity = await page.getByTestId('live-Inbox-message').textContent();
  const draft = page.getByRole('textbox', { name: 'Inbox message live draft', exact: true });
  await draft.fill('Modal-safe live draft');
  await page.getByRole('button', { name: 'Request back', exact: true }).click();
  const dialog = page.getByRole('dialog', { name: 'Leave this entry?', exact: true });
  await expect(dialog).toBeVisible();
  await expect(dialog.getByRole('button', { name: 'Keep editing' })).toBeFocused();
  expect((await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa']).analyze()).violations).toEqual([]);
  if (browserName === 'chromium') {
    mkdirSync('build/screenshots/dom', { recursive: true });
    await page.screenshot({ path: 'build/screenshots/dom/navigation-overlay.png' });
  }
  await page.keyboard.press('Escape');
  await expect(dialog).toHaveCount(0);
  await expect(draft).toHaveValue('Modal-safe live draft');
  await page.getByRole('button', { name: 'Request back', exact: true }).click();
  await dialog.getByRole('button', { name: 'Leave entry' }).click();
  await expect(dialog).toHaveCount(0);
  await page.getByRole('button', { name: 'Open Inbox live message' }).click();
  await expect(page.getByTestId('live-Inbox-message')).toHaveText(identity!);
  await expect(draft).toHaveValue('Modal-safe live draft');
  if (browserName === 'chromium') await page.screenshot({ path: 'build/screenshots/dom/navigation-policies.png' });
});

test('Wasm route overlay preserves its underlying live draft through assistive activation', async ({ page, browserName }) => {
  test.skip(browserName !== 'chromium', 'Wasm interactions are currently verified in Chromium.');
  await page.goto('/wasm/#/navigation-policies');
  const activate = async (name: string) => page.getByRole('button', { name, exact: true })
    .evaluate((element: HTMLElement) => element.click());
  await expect(page.getByRole('button', { name: 'Open Inbox live message', exact: true })).toBeAttached({ timeout: 60000 });
  await activate('Open Inbox live message');
  const draft = page.getByRole('textbox', { name: /^Inbox message live draft/ });
  await draft.focus();
  await expect(page.locator('.compose-backing-field')).toBeFocused();
  await page.keyboard.insertText('Wasm retained draft');
  await expect(draft).toHaveText('Wasm retained draft');
  await activate('Request back');
  const dialog = page.getByRole('dialog');
  await expect(dialog).toBeAttached();
  await expect(dialog).toHaveAttribute('aria-modal', 'true');
  await activate('Keep editing');
  await expect(dialog).toHaveCount(0);
  await expect(draft).toHaveText('Wasm retained draft');
  await activate('Request back');
  await activate('Leave entry');
  await expect(dialog).toHaveCount(0);
  await activate('Open Inbox live message');
  await expect(draft).toHaveText('Wasm retained draft');
});
