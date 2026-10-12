import { test, expect, Page, Locator } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import { mkdirSync, writeFileSync } from 'node:fs';

async function activate(control: Locator) {
  // Compose's documented accessibility bridge invokes OnClick for a DOM click.
  // Its mirror has pointer-events:none; this exercises assistive activation.
  await control.evaluate((element: HTMLElement) => element.click());
}

async function edit(page: Page, label: string, value: string, password = false) {
  const field = page.getByRole('textbox', { name: new RegExp(`^${label}(?:\\s|$)`) });
  await field.focus();
  const backing = page.locator('.compose-backing-field');
  await expect(backing).toBeFocused();
  await expect(backing).toHaveAttribute('inputmode', password ? 'password' : 'text');
  await expect(backing).toHaveText('');
  await page.keyboard.insertText(value);
  await expect(field).toHaveText(password ? '•'.repeat(value.length) : value);
  return field;
}

test.describe('Wasm input and semantics', () => {
  test.beforeEach(async ({ page, browserName }) => {
    test.skip(browserName !== 'chromium', 'Wasm interaction support is currently verified in Chromium.');
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto('/wasm/#/inputs');
    // First-frame readiness precedes Compose's debounced semantics publication.
    await expect(page.getByRole('textbox', { name: 'Display name', exact: true })).toHaveCount(1, { timeout: 60000 });
  });

  test('Unicode editing and assistive activation update the shared feature', async ({ page }) => {
    await expect(page.getByRole('button', { name: 'Preview', exact: true })).toBeDisabled();
    await edit(page, 'Display name', 'Žąsis 李');
    const consent = page.getByRole('checkbox', { name: 'Allow local preview', exact: true });
    await activate(consent);
    await expect(consent).toBeChecked();
    await expect(page.getByRole('button', { name: 'Preview', exact: true })).toBeEnabled();
    await activate(page.getByRole('button', { name: 'Preview', exact: true }));
    await expect(page.getByText('Preview ready for Žąsis 李', { exact: true })).toHaveCount(1);
  });

  test('Tab traversal reaches the native editor and keyboard text updates the field', async ({ page }) => {
    await page.locator('canvas').focus();
    // The reviewed phone layout orders header switch, shop action, then name.
    for (let tab = 0; tab < 3; tab++) {
      await page.keyboard.press('Tab');
      await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => requestAnimationFrame(() => resolve()))));
    }
    const backing = page.locator('.compose-backing-field');
    await expect(backing).toBeFocused();
    await expect(backing).toHaveAttribute('inputmode', 'text');
    await page.keyboard.insertText('Keyboard reached name');
    await expect(page.locator('#input-name')).toHaveText('Keyboard reached name');
  });

  test('validation and disabled semantics follow callbacks without losing edits', async ({ page }) => {
    const consent = page.getByRole('checkbox', { name: 'Allow local preview', exact: true });
    await activate(consent);
    await expect(consent).toBeChecked();
    await expect(page.getByRole('button', { name: 'Preview', exact: true })).toBeEnabled();
    await activate(page.getByRole('button', { name: 'Preview', exact: true }));
    await expect(page.locator('#input-name')).toHaveAttribute('aria-label', /Enter your display name/);
    // The pinned bridge exposes supporting error text in the name, but does not
    // export Compose's Error semantics as aria-invalid; this gap is documented.
    await expect(page.locator('#input-name')).not.toHaveAttribute('aria-invalid');
    await edit(page, 'Display name', 'Retained edit');
    const editing = page.getByRole('switch', { name: 'Editing enabled', exact: true });
    await activate(editing);
    await expect(page.locator('#input-name')).toBeDisabled();
    await expect(consent).toBeDisabled();
    await activate(consent);
    await expect(consent).toBeChecked();
    await expect(page.getByRole('button', { name: 'Preview', exact: true })).toBeDisabled();
    await activate(editing);
    await expect(page.locator('#input-name')).toBeEnabled();
    await expect(page.locator('#input-name')).toHaveText('Retained edit');
  });

  test('password semantics are masked and the private field is not saved in browser history', async ({ page }) => {
    await edit(page, 'Private phrase', 'Fake password 123', true);
    expect(await page.locator('#cmp_a11y_root').ariaSnapshot()).not.toContain('Fake password 123');
    await activate(page.getByRole('tab', { name: 'Tasks', exact: true }));
    await expect(page).toHaveURL(/#\/tasks$/);
    const stored = await page.evaluate(() => JSON.stringify(history.state));
    expect(stored).not.toContain('Fake password 123');
    await page.goBack();
    await expect(page).toHaveURL(/#\/inputs$/);
    await page.reload();
    await expect(page.getByRole('textbox', { name: 'Private phrase', exact: true })).toHaveText('');
  });

  test('modal semantics make the background inert and both action and Escape dismiss it', async ({ page }) => {
    const open = page.getByRole('button', { name: 'Open help', exact: true });
    await activate(open);
    const dialog = page.getByRole('dialog');
    await expect(dialog).toHaveAttribute('aria-modal', 'true');
    await expect.poll(() => page.locator('#cmp_a11y_root').evaluate(element =>
      [...element.children].map(child => (child as HTMLElement).inert))).toEqual([true, false]);
    await page.waitForFunction(() => performance.getEntriesByType('resource')
      .some(resource => resource.name.includes('/notosanssymbols2/')));
    await page.screenshot({ path: 'build/screenshots/wasm/input-lab-help.png' });
    await activate(dialog.getByRole('button', { name: 'Close help', exact: true }));
    await expect(dialog).toHaveCount(0);
    await expect.poll(() => page.locator('#cmp_a11y_root').evaluate(element =>
      [...element.children].every(child => !(child as HTMLElement).inert))).toBe(true);
    await activate(open);
    await expect(dialog).toHaveCount(1);
    await page.locator('canvas').focus();
    await page.keyboard.press('Escape');
    await expect(dialog).toHaveCount(0);
  });

  test('WCAG audit retains bounded upstream bridge findings and rejects new violations', async ({ page }) => {
    const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa']).analyze();
    const findings = await page.evaluate(violations => violations.flatMap(violation => violation.nodes.map(node => {
      const element = new DOMParser().parseFromString(node.html, 'text/html').body.firstElementChild!;
      return { rule: violation.id, tag: element.tagName, role: element.getAttribute('role'),
        orientation: element.getAttribute('aria-orientation'), text: element.textContent?.trim() || '' };
    })), results.violations);
    // These exact nodes are emitted by the pinned Compose 1.13.0-alpha02 bridge.
    // No axe rule is disabled. A changed/new node or rule requires investigation.
    expect(findings).toEqual([
      { rule: 'aria-allowed-attr', tag: 'DIV', role: null, orientation: 'vertical', text: '' },
      { rule: 'aria-required-attr', tag: 'DIV', role: 'heading', orientation: null, text: '◈ Domposable' },
      { rule: 'aria-required-attr', tag: 'DIV', role: 'heading', orientation: null, text: 'Input and accessibility lab' },
      ...['Tasks', 'Components', 'Recipes'].map(text => ({ rule: 'aria-required-parent', tag: 'DIV', role: 'tab', orientation: null, text })),
      { rule: 'aria-roles', tag: 'CANVAS', role: 'generic', orientation: null, text: '' },
    ]);
    mkdirSync('build/accessibility', { recursive: true });
    writeFileSync('build/accessibility/wasm-input-lab.json', JSON.stringify({
      source: 'Compose 1.13.0-alpha02 semantics bridge', certification: false, findings, axe: results,
    }, null, 2));
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
