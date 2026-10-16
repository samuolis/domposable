import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

test('shop URLs, filters, cart and validated demo checkout', async ({ page }) => {
  await page.goto('/shop/');
  await expect(page.locator('header')).toHaveCount(1);
  await expect(page.locator('main')).toHaveCount(1);
  await expect(page.locator('footer')).toHaveCount(1);
  await page.getByRole('link',{name:'Shop all',exact:true}).click();
  await expect(page).toHaveURL(/\/shop\/collection\/$/);
  await page.getByLabel('Search products').fill('not found');
  await expect(page.getByText('No products match your search.')).toBeVisible();
  await page.getByRole('button',{name:'Clear filters'}).click();
  await page.getByRole('button',{name:'View Canvas weekender',exact:true}).click();
  await expect(page).toHaveURL(/\/shop\/products\/canvas-weekender\/$/);
  await page.reload();
  await expect(page.getByRole('heading',{name:'Canvas weekender',exact:true})).toBeVisible();
  await page.getByRole('button',{name:'Add to bag',exact:true}).click();
  await page.getByRole('button',{name:'View bag',exact:true}).click();
  await expect(page).toHaveURL(/\/shop\/cart\/$/);
  await expect(page.getByText('Total  €94.00', {exact:true})).toBeVisible();
  await page.getByRole('button',{name:'Increase Canvas weekender'}).click();
  await expect(page.getByText('Total  €178.00', {exact:true})).toBeVisible();
  await page.getByRole('button',{name:'Decrease Canvas weekender'}).click();
  await page.getByRole('button',{name:'Continue to checkout'}).click();
  await page.getByRole('button',{name:'Place demo order'}).click();
  await expect(page.getByText('Enter a valid email')).toBeVisible();
  await page.getByLabel('Email address').fill('demo@example.test');
  await page.getByLabel('Full name').fill('Demo Customer');
  await page.getByLabel('Street address').fill('1 Example Street');
  await page.getByLabel('Postal code').fill('00000');
  await page.goBack();
  await expect(page.getByRole('heading',{name:'Your bag'})).toBeVisible();
  await page.goForward();
  await expect(page.getByLabel('Full name')).toHaveValue('Demo Customer');
  const historyLength = await page.evaluate(() => window.history.length);
  await page.getByRole('button',{name:'Place demo order'}).click();
  await expect(page.getByRole('heading',{name:'Thanks for exploring Fieldwork.'})).toBeVisible();
  await expect(page.getByRole('link',{name:'Bag (0)',exact:true})).toBeVisible();
  await expect(page).toHaveURL(/\/shop\/confirmation\/$/);
  await expect.poll(() => page.evaluate(() => window.history.length)).toBe(historyLength);
  await page.goBack();
  await expect(page).toHaveURL(/\/shop\/cart\/$/);
  await expect(page.getByRole('heading',{name:'Your bag'})).toBeVisible();
  await page.goForward();
  await expect(page.getByRole('heading',{name:'Thanks for exploring Fieldwork.'})).toBeVisible();
});

test('shop layout, image resources and accessibility at phone and desktop widths',async({page})=>{
  for(const width of [390,1440]) {
    await page.setViewportSize({width,height:900});
    await page.goto('/shop/collection/');
    await expect(page.locator('main img').first()).toHaveAttribute('data-testid',/-loaded$/);
    const scan = await new AxeBuilder({page}).analyze();
    expect(scan.violations).toEqual([]);
    expect(await page.evaluate(()=>document.documentElement.scrollWidth <= window.innerWidth)).toBe(true);
  }
});

test('missing dynamic shop product reloads through the Pages fallback',async({page})=>{
  await page.goto('/shop/products/not-in-catalog/');
  await expect(page.getByText('This product is unavailable.')).toBeVisible();
  await expect(page).toHaveURL(/\/shop\/products\/not-in-catalog\/$/);
  await page.getByRole('button',{name:'Back to collection'}).click();
  await expect(page).toHaveURL(/\/shop\/collection\/$/);
});

test('playground launcher opens the standalone shop', async ({page}) => {
  await page.goto('/#/tasks');
  await page.getByRole('button',{name:'Shop demo',exact:true}).click();
  await expect(page).toHaveURL(/\/shop\/$/);
  await expect(page.getByRole('heading',{name:'FIELDWORK'})).toBeVisible();
});


test('shop header keeps compact website navigation and current-page links', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 1000 });
  await page.goto('/shop/');
  const navigation = page.getByRole('navigation', { name: 'Shop', exact: true });
  const brand = page.getByRole('heading', { name: 'FIELDWORK', exact: true });
  await expect(navigation).toBeVisible();
  const navBox = await navigation.boundingBox();
  const brandBox = await brand.boundingBox();
  expect(navBox!.width).toBeLessThan(600);
  expect(Math.abs(navBox!.y + navBox!.height / 2 - brandBox!.y - brandBox!.height / 2)).toBeLessThan(30);
  await expect(navigation.getByRole('link', { name: 'Discover', exact: true })).toHaveAttribute('aria-current', 'page');
  await navigation.getByRole('link', { name: 'Shop all', exact: true }).click();
  await expect(page).toHaveURL(/\/shop\/collection\/$/);
  await expect(navigation.getByRole('link', { name: 'Shop all', exact: true })).toHaveAttribute('aria-current', 'page');
});

test('built-in dialog dismissal uses the themed text button', async ({ page }) => {
  await page.goto('/#/inputs');
  await page.getByRole('button', { name: 'Open help', exact: true }).click();
  const dialog = page.getByRole('dialog', { name: 'Input help', exact: true });
  const dismiss = dialog.getByRole('button', { name: 'Close', exact: true });
  await expect(dismiss).toBeEnabled();
  await expect(dismiss).toHaveClass(/d-button-text/);
  await dismiss.click();
  await expect(dialog).toBeHidden();
});
