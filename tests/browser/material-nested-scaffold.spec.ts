import { test, expect } from '@playwright/test';

test('nested screen scaffold stays within its measured height and leaves outer landmarks unique', async ({ page }) => {
  await page.goto('/#/material/scaffold');

  const nestedScaffold = page.getByTestId('material-screen-scaffold');
  await expect(nestedScaffold).toHaveClass(/d-scaffold-nested/);
  const nestedBody = nestedScaffold.locator('.d-scaffold-body');
  const scaffoldBounds = await nestedScaffold.boundingBox();
  const bodyBounds = await nestedBody.boundingBox();

  if (!scaffoldBounds || !bodyBounds) {
    throw new Error('The nested scaffold bounds should be measurable');
  }

  expect(scaffoldBounds.height).toBe(420);
  expect(bodyBounds.y).toBeGreaterThanOrEqual(scaffoldBounds.y);
  expect(bodyBounds.y + bodyBounds.height).toBeLessThanOrEqual(scaffoldBounds.y + scaffoldBounds.height + 1);

  await expect(page.getByRole('main')).toHaveCount(1);
  await expect(page.locator('#main-content')).toHaveCount(1);
  await expect(page.getByRole('link', { name: 'Skip to content' })).toHaveCount(1);

  const gestures = page.getByRole('switch', { name: 'Sheet gestures', exact: true });
  await gestures.uncheck();
  await expect(gestures).not.toBeChecked();
});
