import { test, expect } from '@playwright/test';

test('Material progress and loading indicators render controlled state, expressive shape, and reduced motion', async ({ page }) => {
  await page.goto('/#/material/feedback');

  const circular = page.getByRole('progressbar', { name: 'Download Standard', exact: true });
  await expect(circular).toHaveAttribute('aria-valuenow', '60');
  await expect(circular).toHaveCSS('width', '40px');
  await expect(circular).toHaveCSS('height', '40px');
  await expect(circular.locator('.d-standard-active')).toHaveAttribute('stroke-linecap', 'round');
  await expect(circular.locator('.d-standard-track')).toHaveAttribute('stroke-dashoffset', /^-/);

  const wavyCircle = page.getByRole('progressbar', { name: 'Download Wavy', exact: true });
  await expect(wavyCircle).toHaveAttribute('aria-valuenow', '60');
  await expect(wavyCircle).toHaveClass(/d-progress-wavy-circular/);
  const circularPath = wavyCircle.locator('.d-wave-active');
  await expect(circularPath).toHaveAttribute('stroke-dasharray', '60 100');
  const circularPathGeometry = await circularPath.getAttribute('d');
  expect(circularPathGeometry).toContain('L');
  expect(circularPathGeometry?.length ?? 0).toBeGreaterThan(1000);
  await expect(circularPath).toBeVisible();
  await expect(circularPath).toHaveCSS('stroke', /rgb\(/);

  const wavyLinear = page.getByRole('progressbar', { name: 'Linear download Wavy', exact: true });
  await expect(wavyLinear).toHaveAttribute('aria-valuenow', '60');
  await expect(wavyLinear).toHaveClass(/d-progress-wavy-linear/);
  const linearPath = wavyLinear.locator('.d-wave-active');
  await expect(linearPath).not.toHaveAttribute('stroke-dasharray');
  const clipping = await linearPath.evaluate(element => getComputedStyle(element).clipPath);
  expect(Number(clipping.match(/inset\(-50% ([\d.]+)%/)?.[1])).toBeCloseTo(40, 3);
  const linearPathGeometry = await linearPath.getAttribute('d');
  expect(linearPathGeometry).toContain('L');
  expect(linearPathGeometry?.length ?? 0).toBeGreaterThan(500);
  const lineBounds = await linearPath.boundingBox();
  const hostBounds = await wavyLinear.boundingBox();
  expect(lineBounds!.width).toBeGreaterThan(hostBounds!.width * 0.9);

  const uncontained = page.getByRole('progressbar', { name: 'Expressive loading', exact: true });
  const contained = page.getByRole('progressbar', { name: 'Contained loading', exact: true });
  for (const indicator of [uncontained, contained]) {
    await expect(indicator).toHaveAttribute('aria-valuenow', '60');
    const determinateShape = indicator.locator('.d-loading-determinate');
    await expect(determinateShape).toBeVisible();
    await expect(determinateShape).toHaveAttribute('data-progress', '0.6');
    expect(await determinateShape.getAttribute('stroke-dasharray')).toBeNull();
    await expect(determinateShape).toHaveCSS('fill', /rgb\(/);
    await expect(determinateShape).toHaveCSS('stroke', 'none');
    const polygon = await determinateShape.getAttribute('d');
    expect(polygon).toContain('C');
    expect(polygon).toContain('Z');
  }
  await expect(uncontained).toHaveClass(/d-loading-uncontained/);
  await expect(contained).toHaveClass(/d-loading-contained/);

  await page.getByRole('switch', { name: 'Animate loading indicators', exact: true }).check();
  for (const indicator of [uncontained, contained]) {
    await expect(indicator).not.toHaveAttribute('aria-valuenow');
    await expect(indicator).toHaveAttribute('aria-valuetext', /loading/i);
    await expect(indicator.locator('.d-loading-morph-shape').first()).toBeVisible();
  }
  await expect(uncontained.locator('.d-loading-morph-0')).not.toHaveCSS('animation-name', 'none');

  await page.emulateMedia({ reducedMotion: 'reduce' });
  await expect(uncontained.locator('.d-loading-morph-0')).toHaveCSS('animation-name', 'none');
  await expect(contained.locator('.d-loading-morph-0')).toHaveCSS('animation-name', 'none');
});

test('circular wave paints only its controlled progress arc', async ({ page }) => {
  await page.goto('/#/material/compare/light/feedback');
  const indicator = page.getByRole('progressbar', { name: 'Download Wavy', exact: true });
  const points = await indicator.locator('.d-wave-active').evaluate(element => {
    const path = element as SVGPathElement;
    const host = path.closest('.d-progress-wavy')!.getBoundingClientRect();
    return [0.3, 0.8].map(fraction => {
      const point = path.getPointAtLength(path.getTotalLength() * fraction).matrixTransform(path.getScreenCTM()!);
      return { x: point.x - host.x, y: point.y - host.y };
    });
  });
  const screenshot = await indicator.screenshot({ scale: 'css' });
  await test.info().attach('controlled-wave-paint', { body: screenshot, contentType: 'image/png' });
  const painted = await page.evaluate(async ({ image, points }) => {
    const bitmap = new Image();
    bitmap.src = `data:image/png;base64,${image}`;
    await bitmap.decode();
    const canvas = document.createElement('canvas');
    canvas.width = bitmap.width;
    canvas.height = bitmap.height;
    const context = canvas.getContext('2d')!;
    context.drawImage(bitmap, 0, 0);
    return points.map(({ x, y }) => {
      const pixels = context.getImageData(Math.round(x) - 1, Math.round(y) - 1, 3, 3).data;
      for (let offset = 0; offset < pixels.length; offset += 4) {
        if ([87, 70, 178].every((channel, index) => Math.abs(pixels[offset + index] - channel) <= 12)) return true;
      }
      return false;
    });
  }, { image: screenshot.toString('base64'), points });
  expect(painted).toEqual([true, false]);
});
