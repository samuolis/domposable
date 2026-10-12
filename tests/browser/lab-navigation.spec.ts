import { test, expect } from '@playwright/test';

test('component menu exposes collection and input labs with owned routes', async ({ page }) => {
  await page.goto('/#/components');
  await page.getByRole('button', { name: 'Large collections lab', exact: true }).click();
  await expect(page).toHaveURL(/#\/collections$/);
  await page.getByRole('button', { name: 'More collection recipes', exact: true }).click();
  await expect(page).toHaveURL(/#\/collection-recipes$/);
  await expect(page.getByRole('heading', { name: 'Collection recipes', exact: true })).toBeVisible();
  await page.goBack();
  await page.goBack();
  await page.getByRole('button', { name: 'Input and accessibility lab', exact: true }).click();
  await expect(page).toHaveURL(/#\/inputs$/);
  await expect(page.getByRole('textbox', { name: 'Display name', exact: true })).toBeVisible();
});
