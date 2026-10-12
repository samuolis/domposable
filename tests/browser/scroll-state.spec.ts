import { expect, test } from '@playwright/test';

const status = '[data-testid="product-scroll-status"]';

function scrollWindow(page: import('@playwright/test').Page) {
  return page.locator('[data-domposable-modifier="StatefulScroll"]')
    .filter({ has: page.getByTestId('product-scroll-window') });
}

async function openScrollLab(page: import('@playwright/test').Page) {
  await page.goto('/#/product-components');
  await expect(page.getByRole('heading', { name: 'Documents and data lab', exact: true })).toBeVisible();
  await expect(scrollWindow(page)).toBeVisible();
  await expect(scrollWindow(page)).toHaveAttribute('tabindex', '0');
  await expect.poll(() => scrollWindow(page).evaluate(element => (element as HTMLElement).scrollTop)).toBe(80);
}


async function interruptScrollAtFirstMovement(page: import('@playwright/test').Page, controlName: string) {
  await page.getByRole('button', { name: controlName, exact: true }).evaluate(control => {
    const viewport = document.querySelector<HTMLElement>(
      '[data-domposable-modifier="StatefulScroll"]:has([data-testid="product-scroll-window"])',
    )!;
    const initial = viewport.scrollTop;
    const interrupt = () => {
      if (viewport.scrollTop <= initial) return;
      viewport.removeEventListener('scroll', interrupt);
      (control as HTMLButtonElement).click();
    };
    viewport.addEventListener('scroll', interrupt);
  });
}

test('initial offsets restore after layout and a mounted scroll state can be replaced', async ({ page }) => {
  await openScrollLab(page);
  await expect(page.getByTestId('product-scroll-position')).toHaveText('Scroll position: 80');
  await page.getByRole('button', { name: 'Replace scroll state', exact: true }).click();
  await expect.poll(() => scrollWindow(page).evaluate(element => (element as HTMLElement).scrollTop)).toBe(130);
  await expect(page.getByTestId('product-scroll-position')).toHaveText('Scroll position: 130');
});

test('empty content clamps the mounted state and later content accepts a new request', async ({ page }) => {
  await openScrollLab(page);
  await page.getByRole('button', { name: 'Reset empty scroll area', exact: true }).click();
  await expect(page.getByTestId('product-scroll-position')).toHaveText('Scroll position: 0');
  await expect.poll(() => scrollWindow(page).evaluate(element => (element as HTMLElement).scrollTop)).toBe(0);
  await page.getByRole('button', { name: 'Load scroll content', exact: true }).click();
  await expect(scrollWindow(page).getByText('Scrollable item 24', { exact: true })).toHaveCount(1);
  await expect(page.getByTestId('product-scroll-position')).toHaveText('Scroll position: 0');
  await page.getByRole('button', { name: 'Animate down', exact: true }).click();
  await expect(page.locator(status)).toHaveText('Scroll animation completed');
  await expect.poll(() => scrollWindow(page).evaluate(element => (element as HTMLElement).scrollTop)).toBe(520);
});

test('a new animated target cancels and replaces the prior request', async ({ page }) => {
  await openScrollLab(page);
  await page.getByRole('button', { name: 'Animate down', exact: true }).click();
  await expect.poll(() => scrollWindow(page).evaluate(element => (element as HTMLElement).scrollTop)).toBeGreaterThan(80);
  await page.getByRole('button', { name: 'Animate up', exact: true }).click();
  await expect(page.locator(status)).toHaveText('Scroll animation completed');
  await expect.poll(() => scrollWindow(page).evaluate(element => (element as HTMLElement).scrollTop)).toBe(0);
});

test('HTML animateScrollTo moves through intermediate positions and completes at its target', async ({ page }) => {
  await openScrollLab(page);
  await page.evaluate(() => {
    const viewport = document.querySelector<HTMLElement>(
      '[data-domposable-modifier="StatefulScroll"]:has([data-testid="product-scroll-window"])',
    )!;
    const state = { samples: [] as number[], recording: true };
    (window as typeof window & { __domposableScrollSamples?: typeof state }).__domposableScrollSamples = state;
    const sample = () => {
      state.samples.push(viewport.scrollTop);
      if (state.recording) requestAnimationFrame(sample);
    };
    requestAnimationFrame(sample);
  });

  await page.getByRole('button', { name: 'Animate down', exact: true }).click();
  await expect(page.locator(status)).toHaveText('Scroll animation completed');
  const samples = await page.evaluate(() => {
    const state = (window as typeof window & { __domposableScrollSamples?: { samples: number[]; recording: boolean } })
      .__domposableScrollSamples!;
    state.recording = false;
    return state.samples;
  });
  const target = await scrollWindow(page).evaluate(element => (element as HTMLElement).scrollTop);
  expect(target).toBeGreaterThan(0);
  expect(samples.some(value => value > 0 && value < target)).toBe(true);

  const horizontal = page.locator('[data-domposable-modifier="StatefulScroll"]')
    .filter({ has: page.getByTestId('product-horizontal-scroll-window') });
  await horizontal.evaluate(element => { (element as HTMLElement).style.direction = 'rtl'; });
  await page.getByRole('button', { name: 'Scroll right', exact: true }).click();
  const logicalRtlOffset = await horizontal.evaluate(element => {
    const viewport = element as HTMLElement;
    const probe = document.createElement('div');
    const content = document.createElement('div');
    probe.dir = 'rtl';
    probe.style.cssText = 'position:absolute;width:4px;height:1px;overflow:scroll;visibility:hidden';
    content.style.cssText = 'width:8px;height:1px';
    probe.appendChild(content);
    document.body.appendChild(probe);
    const initial = probe.scrollLeft;
    let model: 'default' | 'negative' | 'reverse';
    if (initial > 0) model = 'default';
    else {
      probe.scrollLeft = 1;
      model = probe.scrollLeft === 0 ? 'negative' : 'reverse';
    }
    probe.remove();
    const max = viewport.scrollWidth - viewport.clientWidth;
    if (model === 'default') return max - viewport.scrollLeft;
    if (model === 'negative') return -viewport.scrollLeft;
    return viewport.scrollLeft;
  });
  expect(logicalRtlOffset).toBe(180);
  await page.evaluate(() => new Promise<void>(resolve => requestAnimationFrame(() => requestAnimationFrame(() => resolve()))));
  await expect(page.getByTestId('product-horizontal-scroll-position')).toHaveText('Horizontal scroll position: 180');
});

test('reduced motion makes animateScrollTo immediate and user scroll remains synchronized', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await openScrollLab(page);
  const initial = await scrollWindow(page).evaluate(element => (element as HTMLElement).scrollTop);
  await page.evaluate(() => {
    const viewport = document.querySelector<HTMLElement>(
      '[data-domposable-modifier="StatefulScroll"]:has([data-testid="product-scroll-window"])',
    )!;
    const trace = { values: [] as number[], recording: true };
    (window as typeof window & { __reducedScrollTrace?: typeof trace }).__reducedScrollTrace = trace;
    const sample = () => {
      trace.values.push(viewport.scrollTop);
      if (trace.recording) requestAnimationFrame(sample);
    };
    requestAnimationFrame(sample);
  });
  await page.getByRole('button', { name: 'Animate down', exact: true }).click();
  await expect(page.locator(status)).toHaveText('Scroll animation completed');
  await expect.poll(() => scrollWindow(page).evaluate(element => (element as HTMLElement).scrollTop))
    .toBeGreaterThan(0);
  const trace = await page.evaluate(() => {
    const trace = (window as typeof window & {
      __reducedScrollTrace?: { values: number[]; recording: boolean };
    }).__reducedScrollTrace!;
    trace.recording = false;
    return { values: trace.values, final: (document.querySelector(
      '[data-domposable-modifier="StatefulScroll"]:has([data-testid="product-scroll-window"])',
    ) as HTMLElement).scrollTop };
  });
  expect(trace.final).toBeGreaterThan(initial);
  expect(trace.values.every(value => value === initial || value === trace.final)).toBe(true);

  const viewport = scrollWindow(page);
  const beforeWheel = await viewport.evaluate(element => (element as HTMLElement).scrollTop);
  await viewport.hover();
  await page.mouse.wheel(0, 110);
  await expect.poll(() => viewport.evaluate(element => (element as HTMLElement).scrollTop))
    .toBeGreaterThan(beforeWheel);
  await expect.poll(async () => {
    const position = await viewport.evaluate(element => (element as HTMLElement).scrollTop);
    return page.getByTestId('product-scroll-position').textContent().then(text => text === `Scroll position: ${Math.trunc(position)}`);
  }).toBe(true);
  const beforeKeyboard = await viewport.evaluate(element => (element as HTMLElement).scrollTop);
  await viewport.focus();
  await viewport.press('ArrowUp');
  await expect.poll(() => viewport.evaluate(element => (element as HTMLElement).scrollTop)).toBeLessThan(beforeKeyboard);
});

test('cancelling or disposing a scroll container stops its active browser animation', async ({ page }) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  await openScrollLab(page);
  // Trigger the real UI action at the first scroll frame, so cancellation is exercised
  // while the animation is active even when a busy CI worker delays protocol messages.
  await interruptScrollAtFirstMovement(page, 'Cancel scroll');
  await page.getByRole('button', { name: 'Animate down', exact: true }).click();
  await expect(page.locator(status)).toHaveText('Scroll animation cancelled');
  const cancelledPosition = await scrollWindow(page).evaluate(element => (element as HTMLElement).scrollTop);
  expect(cancelledPosition).toBeGreaterThan(80);
  expect(cancelledPosition).toBeLessThan(520);
  await page.waitForTimeout(200);
  expect(await scrollWindow(page).evaluate(element => (element as HTMLElement).scrollTop))
    .toBeCloseTo(cancelledPosition, 0);

  await page.getByRole('button', { name: 'Scroll to top', exact: true }).click();
  await expect.poll(() => scrollWindow(page).evaluate(element => (element as HTMLElement).scrollTop)).toBe(0);
  await interruptScrollAtFirstMovement(page, 'Remove scroll container');
  await page.getByRole('button', { name: 'Animate down', exact: true }).click();
  await expect(scrollWindow(page)).toHaveCount(0);
  await expect(page.locator(status)).toHaveText('Scroll animation cancelled');
  await page.waitForTimeout(200);
  expect(errors).toEqual([]);
});
