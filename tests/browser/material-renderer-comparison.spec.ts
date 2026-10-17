import { test, expect, Page } from '@playwright/test';
import { mkdir, writeFile } from 'node:fs/promises';

const themes = ['light', 'dark', 'brand-light', 'brand-dark', 'overrides-light', 'overrides-dark', 'expressive-light', 'expressive-dark'];
const families = ['states', 'actions', 'selection', 'inputs', 'surfaces', 'navigation', 'feedback', 'gestures', 'scaffold'];

test.skip(({ browserName }) => browserName !== 'chromium', 'Cross-renderer pixels use pinned Chromium; control behavior retains three-engine coverage.');

for (const theme of themes) {
  for (const family of families) {
    for (const width of [390, 1280]) {
      test(`renderer comparison ${theme} ${family} ${width}`, async ({ browser }) => {
        test.setTimeout(90000);
        const context = await browser.newContext({ viewport: { width, height: 1800 }, deviceScaleFactor: 1, locale: 'en-US', timezoneId: 'UTC' });
        const route = `/material/compare/${theme}/${family}`;
        const directory = `build/renderer-comparison/${theme}-${family}-${width}`;
        await mkdir(directory, { recursive: true });
        const errors: string[] = [];
        try {
          for (const renderer of ['html', 'wasm']) {
            const page = await context.newPage();
            page.on('pageerror', error => errors.push(`${renderer}: ${error.message}`));
            await page.goto(`${renderer === 'wasm' ? '/wasm/' : '/'}#${route}`);
            if (renderer === 'wasm') {
              await expect(page.locator('body')).toHaveAttribute('data-domposable-ready', 'true', { timeout: 60000 });
              await expect(page.locator('body')).toHaveAttribute('data-domposable-initial-route', route);
              await expect(page.locator('canvas').first()).toBeVisible();
            } else {
              await expect(page.getByTestId('material-comparison')).toBeVisible();
              await expect(page.getByRole('heading', { name: `${family} · ${theme}`, exact: true })).toBeVisible();
            }
            await page.evaluate(() => document.fonts.ready);
            await page.clock.install();
            await capture(page, `${directory}/${renderer}.png`);
            if (family === 'states') {
              const control = page.getByRole(renderer === 'wasm' ? 'button' : 'checkbox', { name: 'Enabled unselected checkbox', exact: true });
              const bounds = await control.boundingBox();
              expect(bounds).not.toBeNull();
              await page.clock.resume();
              await page.mouse.move(bounds!.x + bounds!.width / 2, bounds!.y + bounds!.height / 2);
              await capture(page, `${directory}/${renderer}-hover.png`);
              await page.clock.resume();
              await page.mouse.down();
              await capture(page, `${directory}/${renderer}-pressed.png`);
              await page.clock.resume();
              await page.mouse.up();
              await page.mouse.move(0, 0);
              if (renderer === 'wasm') {
                await page.locator('canvas').first().focus();
                await page.keyboard.press('Tab');
              } else {
                await control.focus();
                await expect(control).toBeFocused();
              }
              await capture(page, `${directory}/${renderer}-focus.png`);
            }
            await page.close();
          }
          expect(errors).toEqual([]);
        } finally {
          await context.close();
        }
      });
    }
  }
}


async function capture(page: Page, path: string) {
  const now = await page.evaluate(() => Date.now());
  await page.clock.pauseAt(new Date(now + 1000));
  let previous: Buffer | undefined;
  await expect.poll(async () => {
    const image = await page.screenshot({ animations: 'disabled', caret: 'hide', scale: 'css' });
    const stable = previous?.equals(image) ?? false;
    previous = image;
    return stable;
  }, { timeout: 20000, intervals: [250, 500] }).toBe(true);
  await writeFile(path, previous!);
}
