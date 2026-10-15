import { test, expect } from '@playwright/test';

test('component theme styles the real HTML button and loading retains its dimensions', async ({ page }) => {
  await page.goto('/#/catalog');
  const button = page.getByTestId('themed-action');
  await expect(button).toBeVisible();
  await expect(button).toHaveCSS('background-color', 'rgb(119, 80, 59)');
  await expect(button).toHaveCSS('color', 'rgb(255, 255, 255)');
  await expect(button).toHaveCSS('border-radius', '6px');
  await expect(button).toHaveCSS('min-height', '48px');
  const before = await button.boundingBox();
  await button.click();
  await expect(button).toBeDisabled();
  await expect(button).toHaveAttribute('aria-busy', 'true');
  await expect(button).toHaveAccessibleName('Saving themed action');
  await expect(button).toHaveCSS('background-color', 'rgb(119, 80, 59)');
  const during = await button.boundingBox();
  expect(during?.width).toBe(before?.width);
  expect(during?.height).toBe(before?.height);
  await page.getByRole('button', { name: 'Reset themed action', exact: true }).click();
  await expect(button).toBeEnabled();
  await expect(button).not.toHaveAttribute('aria-busy', 'true');
});

test('themed multiline field floats its label and resizes within its line limits', async ({ page }) => {
  await page.goto('/#/catalog');
  const field = page.getByRole('textbox', { name: 'Theme notes', exact: true });
  await field.scrollIntoViewIfNeeded();
  await expect(field).toHaveJSProperty('tagName', 'TEXTAREA');
  const before = await field.boundingBox();
  await field.fill('First\nSecond\nThird\nFourth\nFifth\nSixth');
  await expect(field).toHaveCSS('border-color', 'rgb(119, 80, 59)');
  await expect.poll(async () => (await field.boundingBox())!.height).toBeGreaterThan(before!.height);
  const metrics = await field.evaluate(element => {
    const style = getComputedStyle(element);
    return { height: element.getBoundingClientRect().height, max: parseFloat(style.lineHeight) * 4
      + parseFloat(style.paddingTop) + parseFloat(style.paddingBottom)
      + parseFloat(style.borderTopWidth) + parseFloat(style.borderBottomWidth) };
  });
  expect(metrics.height).toBeLessThanOrEqual(metrics.max + 1);
  await field.blur();
  await expect(field).toHaveCSS('border-color', 'rgb(116, 121, 140)');
  await expect(field).toHaveAccessibleName('Theme notes');
});

test('theme overrides retain disabled styling and forced-colors keyboard focus', async ({ page }) => {
  await page.goto('/#/catalog');
  const disabled = page.getByRole('button', { name: 'Themed disabled', exact: true });
  await expect(disabled).toBeDisabled();
  await expect(disabled).toHaveCSS('opacity', '1');
  const foreground = await disabled.evaluate(element => getComputedStyle(element).color);
  expect(foreground).toMatch(/^rgba\(28, 32, 48, 0\.38/);
  await page.emulateMedia({ forcedColors: 'active' });
  const action = page.getByTestId('themed-action');
  await action.scrollIntoViewIfNeeded();
  await page.keyboard.press('Tab');
  await action.focus();
  await expect(action).toHaveCSS('outline-style', 'solid');
  await expect(action).toHaveCSS('outline-width', '2px');
});

test('phone autocomplete and keyboard action hints stay on the HTML input', async ({ page }) => {
  await page.goto('/#/catalog');
  const phone = page.getByRole('textbox', { name: 'Contact phone', exact: true });
  await expect(phone).toHaveAttribute('type', 'tel');
  await expect(phone).toHaveAttribute('autocomplete', 'tel');
  await expect(phone).toHaveAttribute('enterkeyhint', 'done');
});
