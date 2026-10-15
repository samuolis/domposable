import { test as base, expect } from '@playwright/test';

const test = base.extend<{ runtimeErrors: void }>({
  runtimeErrors: [async ({ page }, use) => {
    const errors: string[] = [];
    page.on('pageerror', error => errors.push(error.stack || error.message));
    await use();
    expect(errors, 'The Material carousel example must not throw runtime errors').toEqual([]);
  }, { auto: true }],
});

test('multi-browse carousel keeps its preferred slide width and keyboard position', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 1000 });
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.goto('/#/material/gestures');
  await expect(page.getByRole('heading', { name: 'Material catalog', exact: true })).toBeInViewport();

  const carousel = page.getByRole('region', { name: 'MultiBrowse carousel', exact: true });
  const track = carousel.locator('.d-carousel-track');
  const slides = carousel.getByRole('group', { name: /of 6$/ });
  await expect(slides).toHaveCount(6);
  await expect.poll(async () => (await slides.first().boundingBox())?.width).toBeCloseTo(240, 2);

  const position = carousel.locator('[aria-live="polite"]');
  const previous = carousel.getByRole('button', { name: 'Previous item', exact: true });
  const next = carousel.getByRole('button', { name: 'Next item', exact: true });
  await expect(position).toHaveText('1 of 6');
  await track.focus();
  await page.keyboard.press('ArrowRight');
  await expect(position).toHaveText('2 of 6');
  await expect(previous).toBeEnabled();

  await page.keyboard.press('ArrowLeft');
  await expect(position).toHaveText('1 of 6');
  await expect(previous).toBeDisabled();

  await next.click();
  await expect(position).toHaveText('2 of 6');
  await previous.click();
  await expect(position).toHaveText('1 of 6');
  for (let item = 2; item <= 6; item++) {
    await next.click();
    await expect(position).toHaveText(`${item} of 6`);
  }
  await expect(next).toBeDisabled();
  await previous.click();
  await expect(position).toHaveText('5 of 6');
});
