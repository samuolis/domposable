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
