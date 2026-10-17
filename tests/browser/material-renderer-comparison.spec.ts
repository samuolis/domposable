import { test, expect } from '@playwright/test';
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
            await page.clock.pauseAt(new Date(Date.now() + 1000));
            const image = await page.screenshot({ animations: 'disabled', caret: 'hide', scale: 'css' });
            await writeFile(`${directory}/${renderer}.png`, image);
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
