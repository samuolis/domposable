import { test, expect, Page, Locator } from '@playwright/test';
import { mkdir, writeFile } from 'node:fs/promises';

const themes = ['light', 'dark', 'brand-light', 'brand-dark', 'overrides-light', 'overrides-dark', 'expressive-light', 'expressive-dark'];
const families = ['states', 'actions', 'selection', 'inputs', 'surfaces', 'navigation', 'feedback', 'gestures', 'scaffold'];
const controls = [
  { key: 'checkbox', role: 'checkbox', name: 'Enabled unselected checkbox', tabs: 1 },
  { key: 'switch', role: 'switch', name: 'Enabled unselected switch', tabs: 2 },
  { key: 'radio', role: 'radio', name: 'Enabled unselected radio', tabs: 3 },
  // The shared fixture declares seven selection controls before its three continuous sliders.
  { key: 'slider', role: 'slider', name: 'Slider 0.5', tabs: 9 },
] as const;

test.describe.configure({ mode: 'parallel' });

test.skip(({ browserName }) => browserName !== 'chromium', 'Cross-renderer pixels use pinned Chromium; control behavior retains three-engine coverage.');

for (const theme of themes) {
  for (const family of families) {
    for (const width of [390, 1280]) {
      test(`renderer comparison ${theme} ${family} ${width}`, async ({ browser }) => {
        test.setTimeout(180000);
        const route = `/material/compare/${theme}/${family}`;
        const directory = `build/renderer-comparison/${theme}-${family}-${width}`;
        await mkdir(directory, { recursive: true });
        const errors: string[] = [];
        const htmlBounds = new Map<string, { x: number; y: number; width: number; height: number }>();
        const focusColors = new Map<string, string>();
        for (const renderer of ['html', 'wasm'] as const) {
          const context = await browser.newContext({ viewport: { width, height: 1800 }, deviceScaleFactor: 1, locale: 'en-US', timezoneId: 'UTC' });
          try {
            const page = await context.newPage();
            const golden = family === 'states' && width === 390
              ? `build/screenshots/${renderer === 'html' ? 'dom' : 'wasm'}/material-states-${theme}`
              : undefined;
            page.on('pageerror', error => errors.push(`${renderer}: ${error.message}`));
            await page.goto(`${renderer === 'wasm' ? '/wasm/' : '/'}#${route}`);
            await ready(page, renderer, route, family, theme);
            if (renderer === 'html') {
              // Top and bottom captures must cover the whole family, including time/wheel inputs.
              expect(await page.getByTestId('material-comparison').evaluate(element => element.scrollHeight)).toBeLessThanOrEqual(3600);
            }

            await capture(page, `${directory}/${renderer}.png`, golden && `${golden}-rest.png`);
            if (family !== 'states') {
              // Scroll over outer padding so nested calendars and wheels do not consume the event.
              await page.mouse.move(8, 900);
              await page.mouse.wheel(0, 10000);
              await capture(page, `${directory}/${renderer}-end.png`);
            } else {
              for (const target of controls) {
                if (target.key !== 'checkbox') {
                  await page.reload();
                  await ready(page, renderer, route, family, theme);
                }
                const control = renderer === 'wasm' && target.key === 'slider'
                  ? page.getByLabel(target.name, { exact: true }).first()
                  : page.getByRole(renderer === 'wasm' ? 'button' : target.role, { name: target.name, exact: true }).first();
                await expect(control).toBeAttached();
                if (renderer !== 'wasm' || target.key !== 'slider') await expect(control).toBeVisible();
                const measured = renderer === 'wasm' && target.key === 'slider' ? null : await visibleBounds(control);
                if (renderer === 'html') {
                  expect(measured?.width).toBeGreaterThan(0);
                  htmlBounds.set(target.key, measured!);
                }
                // CMP 1.12.2 exposes slider labels without bounds; use the shared fixture's HTML coordinates.
                const bounds = renderer === 'wasm' && target.key === 'slider' ? htmlBounds.get(target.key) : measured;
                expect(bounds?.width).toBeGreaterThan(0);
                const suffix = target.key === 'checkbox' ? '' : `-${target.key}`;
                const targetGolden = target.key === 'checkbox' ? golden : undefined;

                await page.mouse.move(bounds!.x + bounds!.width / 2, bounds!.y + bounds!.height / 2);
                await capture(page, `${directory}/${renderer}${suffix}-hover.png`, targetGolden && `${targetGolden}-hover.png`);

                await page.mouse.down();
                await capture(page, `${directory}/${renderer}${suffix}-pressed.png`, targetGolden && `${targetGolden}-pressed.png`);

                await page.mouse.up();
                await page.mouse.move(0, 0);
                if (renderer === 'wasm') {
                  // Reset pointer focus before traversing the fixture's declared keyboard order.
                  await page.reload();
                  await ready(page, renderer, route, family, theme);
                  await page.bringToFront();
                  const canvas = page.locator('canvas').first();
                  await canvas.focus();
                  await expect(canvas).toBeFocused();
                  await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => requestAnimationFrame(() => resolve()))));
                  for (let index = 0; index < target.tabs; index++) {
                    await page.keyboard.press('Tab');
                    // CMP applies keyboard focus on its next frame, as in the editor traversal test.
                    await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => requestAnimationFrame(() => resolve()))));
                  }
                } else {
                  await control.focus();
                  await page.keyboard.press("Tab");
                  await page.keyboard.press("Shift+Tab");
                  await expect(control).toBeFocused();
                  expect(await control.evaluate(element => element.matches(":focus-visible"))).toBe(true);
                  if (target.key !== 'slider') focusColors.set(target.key, await control.locator('..').evaluate(element => getComputedStyle(element).outlineColor));
                }
                if (renderer === 'wasm' && target.key !== 'slider') {
                  const focusedBounds = await visibleBounds(control);
                  expect(focusedBounds?.width).toBeGreaterThan(0);
                  await expectFocusRing(page, focusedBounds!, focusColors.get(target.key)!);
                }
                await capture(page, `${directory}/${renderer}${suffix}-focus.png`, targetGolden && `${targetGolden}-focus.png`);
              }
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

async function ready(page: Page, renderer: string, route: string, family: string, theme: string) {
  if (renderer === 'wasm') {
    await expect(page.locator('body')).toHaveAttribute('data-domposable-ready', 'true', { timeout: 60000 });
    await expect(page.locator('body')).toHaveAttribute('data-domposable-initial-route', route);
    await expect(page.locator('canvas').first()).toBeVisible();
  } else {
    await expect(page.getByTestId('material-comparison')).toBeVisible();
  }
  await expect(page.getByRole('heading', { name: `${family} · ${theme}`, exact: true })).toBeVisible();
  await page.waitForLoadState('networkidle');
  await page.evaluate(() => document.fonts.ready);
}

async function capture(page: Page, path: string, golden?: string) {
  // The fixture disables indefinite animation; settle real frames without replacing the Wasm clock.
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

// Verify actual canvas paint: the stable CMP bridge does not expose native keyboard focus.
async function expectFocusRing(page: Page, bounds: { x: number; y: number; height: number }, color: string) {
  await expect.poll(async () => {
    const image = (await page.screenshot({ animations: 'disabled', caret: 'hide', scale: 'css' })).toString('base64');
    return page.evaluate(async ({ image, bounds, color }) => {
      const decoded = new Image();
      decoded.src = `data:image/png;base64,${image}`;
      await decoded.decode();
      const canvas = document.createElement('canvas');
      canvas.width = decoded.width;
      canvas.height = decoded.height;
      const context = canvas.getContext('2d')!;
      context.drawImage(decoded, 0, 0);
      // The shared three-pixel ring is three pixels outside the whole hit target.
      const actual = context.getImageData(Math.floor(bounds.x - 5), Math.floor(bounds.y + bounds.height / 2), 1, 1).data;
      context.fillStyle = color;
      context.fillRect(0, 0, 1, 1);
      const expected = context.getImageData(0, 0, 1, 1).data;
      return [0, 1, 2].every(index => Math.abs(actual[index] - expected[index]) <= 12);
    }, { image, bounds, color });
  }, { timeout: 10000, intervals: [100, 250] }).toBe(true);
}

async function visibleBounds(control: Locator) {
  let bounds: { x: number; y: number; width: number; height: number } | null = null;
  // CMP replaces bridge nodes during layout; resolve again if a measured node detached.
  await expect.poll(async () => {
    bounds = await control.boundingBox();
    return !!bounds && bounds.width > 0 && bounds.height > 0;
  }, { timeout: 10000, intervals: [100, 250] }).toBe(true);
  return bounds!;
}
