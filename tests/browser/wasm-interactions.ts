import { expect, Page, Locator } from '@playwright/test';

export async function canvasClick(page: Page, control: Locator) {
  const bounds = await control.boundingBox();
  expect(bounds).not.toBeNull();
  await page.mouse.click(bounds!.x + bounds!.width / 2, bounds!.y + bounds!.height / 2);
}

export async function settledCapture(page: Page) {
  let previous: Buffer | undefined;
  await expect.poll(async () => {
    const current = await page.screenshot();
    const settled = previous?.equals(current) ?? false;
    previous = current;
    return settled;
  }, { intervals: [250] }).toBe(true);
  return previous!;
}
