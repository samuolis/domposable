import { expect, Page } from '@playwright/test';
import { mkdir, writeFile } from 'node:fs/promises';
import { createHash } from 'node:crypto';

export async function stableCapture(page: Page, platform: string, name: string, beforeCapture?: () => Promise<void>) {
  // The playground's diamond/checkmark glyphs use a canvas fallback font loaded after the
  // first frame. document.fonts.ready and a previously reached networkidle do not cover it.
  if (platform === 'wasm' && !name.startsWith('shop')) {
    await page.waitForFunction(() => performance.getEntriesByType('resource')
      .some(resource => resource.name.includes('/notosanssymbols2/')));
    await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => requestAnimationFrame(() => resolve()))));
  }
  await page.evaluate(() => document.fonts.ready);
  if (platform === 'wasm' && name === 'material-navigation' && beforeCapture) {
    // This static scene must settle fallback glyphs before freezing its animation clock.
    let previousFrame = '';
    let stableSince = 0;
    await expect.poll(async () => {
      const frame = createHash('sha256').update(await page.screenshot({ animations: 'disabled', caret: 'hide', scale: 'css' })).digest('hex');
      const now = Date.now();
      if (frame !== previousFrame) stableSince = now;
      previousFrame = frame;
      return now - stableSince >= 1000;
    }, { timeout: 20000, intervals: [250] }).toBe(true);
  }
  await beforeCapture?.();
  let previous = '';
  let capture: Buffer;
  await expect.poll(async () => {
    capture = await page.screenshot({ animations: 'disabled', caret: 'hide', scale: 'css' });
    const hash = createHash('sha256').update(capture).digest('hex');
    const stable = hash === previous;
    previous = hash;
    return stable;
  }, { timeout: 20000, intervals: [250, 500, 1000] }).toBe(true);
  await mkdir(`build/screenshots/${platform}`, { recursive: true });
  await writeFile(`build/screenshots/${platform}/${name}.png`, capture!);
}
