import { test as base, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

const test = base.extend<{ runtimeErrors: void }>({
  runtimeErrors: [async ({ page }, use) => {
    const errors: string[] = [];
    page.on('pageerror', error => errors.push(error.stack || error.message));
    await use();
    expect(errors, 'Catalog sections must mount without runtime errors').toEqual([]);
  }, { auto: true }],
});

for (const section of ['actions', 'selection', 'inputs', 'surfaces', 'navigation', 'feedback', 'gestures', 'scaffold']) {
  for (const width of [390, 1440]) {
    test(`Material ${section} has accessible HTML at ${width}px`, async ({ page }) => {
      await page.setViewportSize({ width, height: 1000 });
      await page.goto(`/#/material/${section}`);
      await expect(page.getByTestId('material-section-heading')).toHaveText(
        section[0].toUpperCase() + section.slice(1),
      );
      expect((await new AxeBuilder({ page }).analyze()).violations).toEqual([]);
    });
  }
}

test('radio groups keep browser arrow-key selection', async ({ page }) => {
  await page.goto('/#/material/selection');
  const personal = page.getByRole('radio', { name: 'Personal', exact: true });
  const business = page.getByRole('radio', { name: 'Business', exact: true });
  await expect(personal).toBeChecked();
  await personal.focus();
  await page.keyboard.press('ArrowDown');
  await expect(business).toBeChecked();
  await expect(personal).not.toBeChecked();
  await page.keyboard.press('ArrowUp');
  await expect(personal).toBeChecked();
});
