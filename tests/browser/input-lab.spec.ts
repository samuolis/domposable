import { test, expect, Page, Locator } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import { mkdirSync, writeFileSync } from 'node:fs';

async function activate(control: Locator) {
  await control.evaluate((element: HTMLElement) => element.click());
}

async function canvasClick(page: Page, control: Locator) {
  const bounds = await control.boundingBox();
  expect(bounds).not.toBeNull();
  await page.mouse.click(bounds!.x + bounds!.width / 2, bounds!.y + bounds!.height / 2);
}

async function edit(page: Page, id: string, value: string, password = false) {
  // Stable CMP publishes test IDs but does not merge field labels into its mirror.
  const field = page.locator(`#${id}`);
  await field.focus();
  const backing = page.locator('.compose-backing-field');
  await expect(backing).toBeFocused();
  await expect(backing).toHaveAttribute('inputmode', password ? 'password' : 'text');
  if (password) await expect(backing).toHaveAttribute('type', 'password');
  await page.keyboard.insertText(value);
  await expect(field).toHaveText(password ? '•'.repeat(value.length) : value);
  return field;
}

test.describe('Stable Wasm comparison and bridge limits', () => {
  test.beforeEach(async ({ page, browserName }) => {
    test.skip(browserName !== 'chromium', 'Wasm interaction support is currently verified in Chromium.');
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto('/wasm/#/inputs');
    await expect(page.locator('#input-name')).toHaveAttribute('role', 'textbox', { timeout: 20000 });
  });

  test('Unicode editing and supported click delegates update the shared feature', async ({ page }) => {
    await edit(page, 'input-name', 'Žąsis 李');
    // CMP 1.12.2 maps all clickable semantics to buttons, including toggles.
    const consent = page.getByRole('button', { name: 'Allow local preview', exact: true });
    await expect(consent).not.toHaveAttribute('aria-checked');
    await activate(consent);
    await activate(page.getByRole('button', { name: 'Preview', exact: true }));
    await expect(page.getByText('Preview ready for Žąsis 李', { exact: true })).toHaveCount(1);
  });

  test('Tab traversal reaches the native editor and keyboard text updates the field', async ({ page }) => {
    await page.locator('canvas').focus();
    const backing = page.locator('.compose-backing-field');
    for (let tab = 0; tab < 8; tab++) {
      await page.keyboard.press('Tab');
      await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => requestAnimationFrame(() => resolve()))));
      if (await backing.count() && await backing.evaluate(element => element.matches(':focus'))) break;
    }
    await expect(backing).toBeFocused();
    await expect(backing).toHaveAttribute('inputmode', 'text');
    await page.keyboard.insertText('Keyboard reached name');
    await expect(page.locator('#input-name')).toHaveText('Keyboard reached name');
  });

  test('native disabled interactions retain edits while the bridge omits disabled state', async ({ page }) => {
    await edit(page, 'input-name', 'Retained edit');
    const consent = page.getByRole('button', { name: 'Allow local preview', exact: true });
    const editing = page.getByRole('button', { name: 'Editing enabled', exact: true });
    await canvasClick(page, consent);
    await canvasClick(page, editing);
    await expect(page.locator('#input-name')).not.toHaveAttribute('aria-disabled');
    await expect(consent).not.toHaveAttribute('aria-disabled');
    // Exercise the actual disabled canvas control, not the incomplete assistive delegate.
    await canvasClick(page, consent);
    await canvasClick(page, editing);
    await expect(page.locator('#input-name')).toHaveText('Retained edit');
    await canvasClick(page, page.getByRole('button', { name: 'Preview', exact: true }));
    await expect(page.getByText('Preview ready for Retained edit', { exact: true })).toHaveCount(1);
  });

  test('password semantics are masked and the private field is not saved in browser history', async ({ page }) => {
    await edit(page, 'input-secret', 'Fake password 123', true);
    expect(await page.locator('body').ariaSnapshot()).not.toContain('Fake password 123');
    await activate(page.getByRole('button', { name: 'Tasks', exact: true }));
    await expect(page).toHaveURL(/#\/tasks$/);
    expect(await page.evaluate(() => JSON.stringify(history.state))).not.toContain('Fake password 123');
    await page.goBack();
    await expect(page).toHaveURL(/#\/inputs$/);
    await page.reload();
    await expect(page.locator('#input-secret')).toHaveText('');
  });

  test('native help actions and Escape dismiss the overlay despite missing modal semantics', async ({ page }) => {
    const open = page.getByRole('button', { name: 'Open help', exact: true });
    const close = page.getByRole('button', { name: 'Close help', exact: true });
    await canvasClick(page, open);
    await expect(close).toHaveCount(1);
    await expect(page.getByRole('dialog')).toHaveCount(0);
    await expect(page.locator('[aria-modal="true"]')).toHaveCount(0);
    await expect.poll(() => page.locator('#cmp_a11y_root').evaluate(element =>
      [...element.children].every(child => !(child as HTMLElement).inert))).toBe(true);
    await page.waitForFunction(() => performance.getEntriesByType('resource')
      .some(resource => resource.name.includes('/notosanssymbols2/')));
    await page.screenshot({ path: 'build/screenshots/wasm/input-lab-help.png' });
    await canvasClick(page, close);
    await expect(close).toHaveCount(0);
    await canvasClick(page, open);
    await expect(close).toHaveCount(1);
    await page.locator('canvas').focus();
    await page.keyboard.press('Escape');
    await expect(close).toHaveCount(0);
  });

  test('WCAG audit records exact stable bridge findings and rejects new violations', async ({ page }) => {
    const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa']).analyze();
    const findings = await page.evaluate(violations => violations.flatMap(violation => violation.nodes.map(node => {
      const element = new DOMParser().parseFromString(node.html, 'text/html').body.firstElementChild!;
      return { rule: violation.id, tag: element.tagName, role: element.getAttribute('role'),
        orientation: element.getAttribute('aria-orientation'), text: element.textContent?.trim() || '' };
    })), results.violations);
    // Export even on failure: a new bridge finding must be inspected, not silently accepted.
    mkdirSync('build/accessibility', { recursive: true });
    writeFileSync('build/accessibility/wasm-input-lab.json', JSON.stringify({
      source: 'Compose 1.12.2 semantics bridge', certification: false, findings, axe: results,
    }, null, 2));
    expect(findings.map(node => JSON.stringify(node)).sort()).toEqual([
      ...Array.from({ length: 2 }, () => ({ rule: 'aria-input-field-name', tag: 'DIV', role: 'textbox', orientation: null, text: '' })),
      { rule: 'aria-roles', tag: 'CANVAS', role: 'generic', orientation: null, text: '' },
      { rule: 'aria-required-attr', tag: 'DIV', role: 'heading', orientation: null, text: '◈ Domposable' },
      { rule: 'aria-required-attr', tag: 'DIV', role: 'heading', orientation: null, text: 'Input and accessibility lab' },
    ].map(node => JSON.stringify(node)).sort());
    await page.waitForFunction(() => performance.getEntriesByType('resource')
      .some(resource => resource.name.includes('/notosanssymbols2/')));
    await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => requestAnimationFrame(() => resolve()))));
    await page.screenshot({ path: 'build/screenshots/wasm/input-lab.png' });
  });
});

test('HTML input lab uses labelled native controls, validation, masked input and modal focus', async ({ page }) => {
  await page.goto('/#/inputs');
  await expect(page.getByRole('heading', { name: 'Input and accessibility lab', exact: true })).toBeVisible();
  if (test.info().project.name === 'chromium') {
    await page.screenshot({ path: 'build/screenshots/dom/input-lab.png' });
  }
  const name = page.getByRole('textbox', { name: /^Display name(?:\s|$)/ });
  const consent = page.getByRole('checkbox', { name: 'Allow local preview', exact: true });
  await expect(page.getByRole('button', { name: 'Preview', exact: true })).toBeDisabled();
  await consent.check();
  await page.getByRole('button', { name: 'Preview', exact: true }).click();
  await expect(name).toHaveAttribute('aria-invalid', 'true');
  await name.fill('Žąsis 李');
  const secret = page.getByLabel('Private phrase', { exact: true });
  await expect(secret).toHaveAttribute('type', 'password');
  await secret.fill('Fake password 123');
  await page.getByRole('button', { name: 'Preview', exact: true }).click();
  await expect(page.getByRole('status')).toHaveText('Preview ready for Žąsis 李');
  const open = page.getByRole('button', { name: 'Open help', exact: true });
  await open.click();
  const dialog = page.getByRole('dialog', { name: 'Input help', exact: true });
  await expect(dialog).toBeVisible();
  if (test.info().project.name === 'chromium') {
    await page.screenshot({ path: 'build/screenshots/dom/input-lab-help.png' });
  }
  await page.keyboard.press('Escape');
  await expect(dialog).toBeHidden();
  await expect(open).toBeFocused();
  expect((await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa']).analyze()).violations).toEqual([]);
});
