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
        const route = `/material/compare/${theme}/${family}`;
        const directory = `build/renderer-comparison/${theme}-${family}-${width}`;
        await mkdir(directory, { recursive: true });
        const errors: string[] = [];
        for (const renderer of ['html', 'wasm']) {
          const context = await browser.newContext({ viewport: { width, height: 1800 }, deviceScaleFactor: 1, locale: 'en-US', timezoneId: 'UTC' });
          try {
            const page = await context.newPage();
            const golden = family === 'states' && width === 390
              ? `build/screenshots/${renderer === 'html' ? 'dom' : 'wasm'}/material-states-${theme}`
              : undefined;
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
            await page.waitForLoadState('networkidle');
            await page.evaluate(() => document.fonts.ready);
            await page.clock.install();
            await capture(page, `${directory}/${renderer}.png`, golden && `${golden}-rest.png`);
            if (family === 'states') {
              const control = page.getByRole(renderer === 'wasm' ? 'button' : 'checkbox', { name: 'Enabled unselected checkbox', exact: true });
              const bounds = await control.boundingBox();
              expect(bounds).not.toBeNull();
              await page.clock.resume();
              await page.mouse.move(bounds!.x + bounds!.width / 2, bounds!.y + bounds!.height / 2);
              await capture(page, `${directory}/${renderer}-hover.png`, golden && `${golden}-hover.png`);
              await page.clock.resume();
              await page.mouse.down();
              await capture(page, `${directory}/${renderer}-pressed.png`, golden && `${golden}-pressed.png`);
              await page.clock.resume();
              await page.mouse.up();
              await page.mouse.move(0, 0);
              if (renderer === 'wasm') {
                // Start keyboard traversal without focus retained from the pointer capture.
                await page.reload();
                await expect(page.locator('body')).toHaveAttribute('data-domposable-ready', 'true', { timeout: 60000 });
                await page.waitForLoadState('networkidle');
                await page.evaluate(() => document.fonts.ready);
                await page.locator('canvas').first().focus();
                await page.keyboard.press('Tab');
              } else {
                await control.focus();
                await expect(control).toBeFocused();
              }
              await capture(page, `${directory}/${renderer}-focus.png`, golden && `${golden}-focus.png`);
            }
          } finally {
            await context.close();
          }
        }
        expect(errors).toEqual([]);
      });
    }
  }
}


async function capture(page: Page, path: string, golden?: string) {
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
  if (golden) {
    await mkdir(golden.slice(0, golden.lastIndexOf('/')), { recursive: true });
    await writeFile(golden, previous!);
  }
}
