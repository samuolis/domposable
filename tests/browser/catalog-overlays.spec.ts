import { expect, test } from '@playwright/test';

for (const kind of ['dialog', 'sheet']) {
  test(`${kind} keeps content clicks open and dismisses on the backdrop`, async ({ page }, testInfo) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto('/#/catalog');
    const trigger = page.getByRole('button', { name: `Open ${kind}`, exact: true });
    await trigger.click();
    const modal = page.getByRole('dialog', { name: `Example ${kind}`, exact: true });
    await expect(modal).toBeVisible();
    await modal.getByRole('heading').click();
    await expect(modal).toBeVisible();
    await page.screenshot({ path: testInfo.outputPath(`catalog-${kind}-phone.png`) });
    await page.mouse.click(4, 4);
    await expect(modal).toHaveCount(0);
    await expect(trigger).toBeFocused();
  });
}

test('catalog menu has an opaque surface above the app header and restores focus', async ({ page }, testInfo) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto('/#/catalog');
  const trigger = page.getByRole('button', { name: 'Open menu', exact: true });
  await trigger.click();
  const menu = page.getByRole('menu');
  await expect(menu).toBeVisible();
  const surface = await menu.evaluate(element => {
    const bounds = element.getBoundingClientRect();
    return {
      background: getComputedStyle(element).backgroundColor,
      aboveHeader: element.contains(document.elementFromPoint(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2)),
    };
  });
  expect(surface.background).not.toMatch(/^(transparent|rgba\(0, 0, 0, 0\))$/);
  expect(surface.aboveHeader).toBe(true);
  await page.screenshot({ path: testInfo.outputPath('catalog-menu-phone.png') });
  await page.mouse.click(4, 4);
  await expect(menu).toHaveCount(0);
  await expect(trigger).toBeFocused();
  await trigger.click();
  await expect(menu).toBeVisible();
  await expect(menu.getByRole('menuitem').first()).toBeFocused();
  await page.keyboard.press('Escape');
  await expect(menu).toHaveCount(0);
  await expect(trigger).toBeFocused();
});

test('action chips remain actions while filter chips expose their controlled selection', async ({ page }) => {
  await page.goto('/#/material/compare/light/selection');
  for (const variant of ['assist', 'suggestion']) {
    const chip = page.locator(`.d-chip-${variant}`).first();
    await expect(chip).toBeVisible();
    await expect(chip).not.toHaveAttribute('aria-pressed', /.+/);
    await chip.click();
    await expect(chip).not.toHaveAttribute('aria-pressed', /.+/);
  }
  await expect(page.locator('.d-chip-filter').first()).toHaveAttribute('aria-pressed', /^(true|false)$/);
});

for (const theme of ['light', 'dark', 'brand-light', 'brand-dark', 'overrides-light', 'overrides-dark', 'expressive-light', 'expressive-dark']) {
  test(`chip interaction paint stays inside its visual surface in ${theme}`, async ({ page }, testInfo) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto(`/#/material/compare/${theme}/selection`);
    const chip = page.locator('.d-chip-filter').first();
    await expect(chip).toBeVisible();
    for (const selected of ['false', 'true']) {
      if (await chip.getAttribute('aria-pressed') !== selected) await chip.click();
      await expect(chip).toHaveAttribute('aria-pressed', selected);
      for (const state of ['rest', 'hover', 'pressed', 'focus']) {
        if (state === 'rest') await page.mouse.move(0, 0);
        else if (state === 'focus') {
          await page.keyboard.press('Tab');
          await chip.focus();
          await expect(chip).toBeFocused();
          await expect.poll(() => chip.evaluate(element => element.matches(':focus-visible'))).toBe(true);
        } else await chip.hover();
        if (state === 'pressed') await page.mouse.down();
        const paint = await chip.evaluate(element => {
          const style = getComputedStyle(element);
          return { background: style.backgroundColor, border: style.borderTopWidth, shadow: style.boxShadow };
        });
        expect(paint.background, `${selected}: ${state}`).toBe('rgba(0, 0, 0, 0)');
        expect(paint.border, `${selected}: ${state}`).toBe('0px');
        expect(paint.shadow, `${selected}: ${state}`).toBe('none');
        if (state === 'pressed') {
          await page.mouse.move(0, 0);
          await page.mouse.up();
        }
      }
    }
    await chip.hover();
    await page.screenshot({ path: testInfo.outputPath(`chips-${theme}-phone.png`) });
  });
}

test('forced colors retain chip selection and a visible keyboard focus ring without painting its touch target', async ({ page }) => {
  await page.emulateMedia({ forcedColors: 'active' });
  await page.goto('/#/material/compare/light/selection');
  const chip = page.locator('.d-chip-filter').first();
  await expect(chip).toBeVisible();
  if (await chip.getAttribute('aria-pressed') !== 'true') await chip.click();
  await page.keyboard.press('Tab');
  await chip.focus();
  await expect(chip).toBeFocused();
  await expect.poll(() => chip.evaluate(element => element.matches(':focus-visible'))).toBe(true);
  const paint = await chip.evaluate(element => {
    const style = getComputedStyle(element);
    return { target: style.backgroundColor, surface: getComputedStyle(element.querySelector('.d-chip-surface')!).backgroundColor, outline: style.outlineStyle };
  });
  expect(paint.target).toMatch(/^rgba\([\d.]+,\s*[\d.]+,\s*[\d.]+,\s*0\)$/);
  expect(paint.surface).not.toMatch(/^(transparent|rgba\(0, 0, 0, 0\))$/);
  expect(paint.outline).toBe('solid');
});
