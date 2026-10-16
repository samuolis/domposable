import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

test('shared card layers overlap and adaptive panes retain the hidden directory', async ({ page }) => {
  await page.setViewportSize({ width: 1280, height: 1000 });
  await page.goto('/#/composition');
  const directory = page.getByRole('region', { name: 'Workshop directory', exact: true });
  const details = page.getByRole('region', { name: 'Workshop details', exact: true });
  await expect(directory).toBeVisible();
  await expect(details).toBeVisible();
  const directoryBounds = await directory.boundingBox();
  const detailBounds = await details.boundingBox();
  expect(directoryBounds!.height).toBeCloseTo(detailBounds!.height, 0);
  expect(directoryBounds!.width).toBeGreaterThanOrEqual(240);
  expect(directoryBounds!.width).toBeLessThanOrEqual(320);

  const directoryNode = await directory.elementHandle();
  const note = page.getByRole('textbox', { name: 'Directory note', exact: true });
  await note.fill('Retain this directory draft');
  const hero = await page.getByTestId('shared-card-hero').boundingBox();
  for (const id of ['shared-card-image', 'shared-card-scrim']) {
    const layer = await page.getByTestId(id).boundingBox();
    expect(layer).not.toBeNull();
    expect(layer!.x).toBeCloseTo(hero!.x, 0);
    expect(layer!.y).toBeCloseTo(hero!.y, 0);
    expect(layer!.width).toBeCloseTo(hero!.width, 0);
    expect(layer!.height).toBeCloseTo(hero!.height, 0);
  }
  const scrim = page.getByTestId('shared-card-scrim').locator('xpath=ancestor-or-self::*[@data-domposable-modifier="GradientBackground"]');
  await expect(scrim).toHaveCSS('background-image', /linear-gradient/);
  await page.getByRole('button', { name: 'Media action', exact: true }).click();
  await expect(page.getByText('Media actions: 1', { exact: true })).toBeVisible();
  await page.getByRole('button', { name: 'Save workshop', exact: true }).click();
  await expect(page.getByRole('button', { name: 'Workshop saved', exact: true })).toBeVisible();
  await page.getByRole('switch', { name: 'Use compact panes', exact: true }).check();
  await expect(directory).toBeHidden();
  await expect(details).toBeVisible();
  await page.getByRole('switch', { name: 'Use compact panes', exact: true }).uncheck();
  await expect(directory).toBeVisible();
  await expect(note).toHaveValue('Retain this directory draft');
  expect(await directoryNode!.evaluate(element => element.isConnected && element === document.querySelector('[role=region][aria-label="Workshop directory"]'))).toBe(true);
  expect((await new AxeBuilder({ page }).analyze()).violations).toEqual([]);
});
