import { test, expect } from '@playwright/test';

test('typed button interaction styles respond to hover, press, focus, and disabled states', async ({ page }) => {
  await page.goto('/#/catalog');
  await page.evaluate(() => {
    const theme = document.querySelector('.d-theme') ?? document.body;
    const root = document.createElement('div');
    root.style.cssText = 'position:fixed;inset:12px auto auto 12px;z-index:99999;display:flex;gap:12px';
    root.innerHTML = `
      <button id="typed" class="d-button d-button-filled" data-d-button-interaction="true"
        style="transition:none;background:#123456;color:#ffffff;--d-button-hovered-container:#112233;--d-button-pressed-content:#445566;--d-button-focus-ring-color:#778899;--d-button-focus-ring-width:4px;--d-button-focus-ring-offset:6px">Typed</button>
      <button id="default" class="d-button d-button-filled" style="transition:none">Default</button>
      <button id="layered" class="d-icon-button d-icon-button-standard" data-d-button-interaction="true"
        style="transition:none;--d-button-hover-feedback:inset 0 0 0 999px color-mix(in srgb,currentColor 9%,transparent);--d-button-pressed-feedback:inset 0 0 0 999px color-mix(in srgb,currentColor 16%,transparent);--d-button-focus-feedback:inset 0 0 0 999px color-mix(in srgb,currentColor 12%,transparent);--d-button-focus-ring-color:#778899;--d-button-focus-ring-width:3px;--d-button-focus-ring-offset:0px">Layered</button>
      <button id="disabled" class="d-button d-button-filled" data-d-button-interaction="true" disabled
        style="--d-button-hovered-container:#112233">Disabled</button>`;
    theme.prepend(root);
    root.id = 'interaction-fixture';
  });
  const typed = page.locator('#typed');
  const defaultButton = page.locator('#default');
  const layered = page.locator('#layered');
  const disabled = page.locator('#disabled');
  await typed.hover();
  await expect(typed).toHaveCSS('background-color', 'rgb(17, 34, 51)');
  await page.mouse.down();
  await expect(typed).toHaveCSS('color', 'rgb(68, 85, 102)');
  await page.mouse.up();
  await typed.focus();
  await page.keyboard.press('Tab');
  await page.keyboard.press('Shift+Tab');
  // Safari's button tab-navigation preference varies; request the target after real keyboard input.
  await typed.focus();
  await expect(typed).toBeFocused();
  await expect(typed).toHaveCSS('outline-width', '4px');
  await expect(typed).toHaveCSS('outline-offset', '6px');
  await layered.hover();
  await expect.poll(() => layered.evaluate((element) => getComputedStyle(element).boxShadow)).toContain('inset');
  await page.mouse.down();
  await expect.poll(() => layered.evaluate((element) => getComputedStyle(element).boxShadow)).toContain('inset');
  await page.mouse.up();
  await layered.focus();
  await page.keyboard.press('Tab');
  await page.keyboard.press('Shift+Tab');
  await layered.focus();
  await expect(layered).toBeFocused();
  await expect(layered).toHaveCSS('outline-color', 'rgb(119, 136, 153)');
  await expect.poll(() => layered.evaluate((element) => getComputedStyle(element).boxShadow)).toContain('inset');
  await defaultButton.hover();
  await expect(defaultButton).toHaveCSS('filter', 'brightness(0.95)');
  await disabled.hover();
  await expect(disabled).not.toHaveCSS('background-color', 'rgb(17, 34, 51)');
  await page.evaluate(() => document.querySelector('#interaction-fixture')?.remove());
});
