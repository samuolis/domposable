import { test, expect } from '@playwright/test';

test('deep link opens a contact and retains edits across back navigation', async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.goto('/#/contact/sam');
  await expect(page.getByRole('heading', { name: 'Sam' })).toBeVisible();
  await page.getByRole('textbox', { name: 'Note' }).fill('Meet at noon');
  await page.getByRole('button', { name: 'Open Alex' }).click();
  await expect(page.getByRole('heading', { name: 'Alex' })).toBeVisible();
  await page.goBack();
  await expect(page.getByRole('heading', { name: 'Sam' })).toBeVisible();
  await expect(page.getByRole('textbox', { name: 'Note' })).toHaveValue('Meet at noon');
  expect(errors).toEqual([]);
});
