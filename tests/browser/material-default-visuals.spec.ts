import { test, expect } from '@playwright/test';

test('expressive theme keeps ordinary card shapes and explicit overrides', async ({ page }) => {
  await page.goto('/#/material/surfaces');
  const card = page.locator('.d-card').filter({ has: page.getByText('Filled card', { exact: true }) });
  await expect(page.getByRole('switch', { name: 'Expressive theme', exact: true })).not.toBeChecked();
  await expect(card).toHaveCSS('border-top-left-radius', '12px');
  await page.getByRole('switch', { name: 'Expressive theme', exact: true }).check();
  await expect(page.locator('.d-theme-expressive')).toBeVisible();
  await expect(card).toHaveCSS('border-top-left-radius', '12px');
  await page.getByRole('switch', { name: 'Component overrides', exact: true }).check();
  await expect(card).toHaveCSS('border-top-left-radius', '20px');
  await page.getByRole('switch', { name: 'Expressive theme', exact: true }).uncheck();
  await expect(card).toHaveCSS('border-top-left-radius', '20px');
});

test('selection controls and chips use Material-sized visual surfaces', async ({ page }) => {
  await page.goto('/#/material/selection');

  const checkbox = page.getByRole('checkbox', { name: 'Receive updates' });
  const radio = page.getByRole('radio', { name: 'Personal' });
  const toggle = page.getByRole('switch', { name: 'Enable notifications' });
  await expect(checkbox).toHaveCSS('appearance', 'none');
  await expect(checkbox).toHaveCSS('width', '20px');
  await expect(radio).toHaveCSS('border-radius', '50%');
  await expect(toggle).toHaveCSS('appearance', 'none');
  await expect(toggle).toHaveCSS('width', '52px');
  await expect(toggle).toHaveCSS('height', '32px');
  await expect(checkbox.locator('..')).toHaveCSS('min-height', '48px');
  const defaultColors = await checkbox.evaluate(control => {
    const theme = getComputedStyle(control.closest('.d-theme')!);
    const resolve = (token: string) => {
      const swatch = document.createElement('span');
      swatch.style.color = theme.getPropertyValue(token).trim();
      return swatch.style.color;
    };
    return {
      outline: getComputedStyle(control).borderTopColor,
      expectedOutline: resolve('--d-on-surface-variant'),
      expectedTrack: resolve('--d-surface-container-highest'),
    };
  });
  expect(defaultColors.outline).toBe(defaultColors.expectedOutline);
  expect(await toggle.evaluate(control => getComputedStyle(control).backgroundColor)).toBe(defaultColors.expectedTrack);

  const filterChip = page.locator('.d-chip-filter').first();
  const chipSurface = (await filterChip.evaluate(element => element.classList.contains('d-item-hittarget')))
    ? filterChip.locator('.d-chip-surface')
    : filterChip;
  await expect(chipSurface).toHaveCSS('min-height', '32px');
  await expect(chipSurface).toHaveCSS('border-top-left-radius', '8px');
});

test('segmented controls apply theme geometry without shrinking the hit target', async ({ page }) => {
  await page.goto('/#/material/selection');
  await page.getByRole('switch', { name: 'Component overrides', exact: true }).check();
  const group = page.getByRole('radiogroup', { name: 'Period', exact: true });
  const segment = group.locator('label').first();
  const surface = segment.locator('.d-segment-surface');
  await expect(segment).toHaveCSS('min-height', '64px');
  await expect(surface).toHaveCSS('min-height', '40px');
  await expect(surface).toHaveCSS('padding', '8px 12px');
  await expect(surface).toHaveCSS('gap', '8px');
  await expect(surface).toHaveCSS('border-top-width', '3px');
});

test('text fields, badges, and progress use Material default dimensions', async ({ page }) => {
  await page.goto('/#/material/inputs');
  const field = page.getByRole('textbox', { name: 'Outlined field' });
  const fieldControl = page.locator('.d-field-control').filter({ has: field });
  await expect(fieldControl).toHaveCSS('min-height', '56px');
  await expect(fieldControl).toHaveCSS('border-top-left-radius', '4px');
  await expect(field).toHaveCSS('padding-top', '16px');
  const withoutLabel = page.getByRole('textbox', { name: 'Search documents', exact: true });
  await expect(withoutLabel).toHaveAttribute('placeholder', 'Search documents');
  const placeholderColor = await withoutLabel.evaluate(element => getComputedStyle(element, '::placeholder').color);
  expect(placeholderColor).not.toBe('rgba(0, 0, 0, 0)');

  await page.goto('/#/material/feedback');
  const badge = page.locator('.d-badged-box .d-badge');
  const badgeBounds = await badge.boundingBox();
  if (!badgeBounds) throw new Error('The badge should be visible');
  expect(badgeBounds.height).toBe(16);
  expect(badgeBounds.width).toBeGreaterThanOrEqual(16);
  expect(badgeBounds.width).toBeLessThan(24);

  const linearProgress = page.locator('.d-progress-standard:not(.d-progress-circular)').first();
  const progressBounds = await linearProgress.boundingBox();
  if (!progressBounds) throw new Error('The linear progress indicator should be visible');
  expect(progressBounds.height).toBe(4);
  expect(progressBounds.width).toBeGreaterThan(400);

  const determinateLoadingShape = page.locator('.d-loading-indicator .d-loading-determinate').first();
  const loadingPaint = await determinateLoadingShape.evaluate(element => {
    const path = element as SVGPathElement;
    const svg = path.ownerSVGElement;
    const box = path.getBBox();
    const clientBox = path.getBoundingClientRect();
    const host = path.closest('.d-loading-indicator')!.getBoundingClientRect();
    return {
      fill: getComputedStyle(path).fill,
      opacity: getComputedStyle(path).opacity,
      width: box.width,
      height: box.height,
      clientWidth: clientBox.width,
      clientHeight: clientBox.height,
      svgWidth: svg?.getBoundingClientRect().width ?? 0,
      svgHeight: svg?.getBoundingClientRect().height ?? 0,
      leftInset: clientBox.left - host.left,
      rightInset: host.right - clientBox.right,
      topInset: clientBox.top - host.top,
      bottomInset: host.bottom - clientBox.bottom,
    };
  });
  expect(loadingPaint.fill).not.toBe('none');
  expect(Number(loadingPaint.opacity)).toBeGreaterThan(0);
  expect(loadingPaint.width).toBeGreaterThan(0);
  expect(loadingPaint.height).toBeGreaterThan(0);
  expect(loadingPaint.clientWidth).toBeGreaterThan(0);
  expect(loadingPaint.clientHeight).toBeGreaterThan(0);
  expect(loadingPaint.svgWidth).toBeGreaterThan(0);
  expect(loadingPaint.svgHeight).toBeGreaterThan(0);
  for (const inset of [loadingPaint.leftInset, loadingPaint.rightInset, loadingPaint.topInset, loadingPaint.bottomInset]) {
    expect(inset).toBeGreaterThanOrEqual(-1);
  }
});

test('horizontal Material navigation uses a full-width stacked bar', async ({ page }) => {
  await page.goto('/#/material/navigation');

  const navigation = page.getByRole('navigation', { name: 'Bottom navigation example' });
  await expect(navigation).toHaveCSS('min-height', '80px');
  await expect(navigation.locator('button, a').first()).toHaveCSS('flex-direction', 'column');
  const bounds = await navigation.boundingBox();
  if (!bounds) throw new Error('The bottom navigation should be visible');
  expect(bounds.width).toBeGreaterThan(400);
});

test('unchecked filled icon toggles use the neutral Material toggle surface', async ({ page }) => {
  await page.goto('/#/material/actions');

  const filled = page.getByRole('button', { name: 'Favorite Filled', exact: true });
  const tonal = page.getByRole('button', { name: 'Favorite FilledTonal', exact: true });
  await expect(filled).toHaveAttribute('aria-pressed', 'false');
  await expect(tonal).toHaveAttribute('aria-pressed', 'false');

  const surfaces = await page.locator('.d-theme').last().evaluate(theme => {
    const styles = getComputedStyle(theme);
    const color = document.createElement('span').style;
    color.color = styles.getPropertyValue('--d-surface-container-highest').trim();
    return {
      neutral: color.color,
      filled: getComputedStyle(document.querySelector('.d-icon-button-filled')!).backgroundColor,
      tonal: getComputedStyle(document.querySelector('.d-icon-button-filled-tonal')!).backgroundColor,
    };
  });
  expect(surfaces.neutral).not.toBe('');
  expect(surfaces.filled).toBe(surfaces.neutral);
  expect(surfaces.tonal).toBe(surfaces.neutral);
});


test('disabled button variants use Material state colors without fading the entire control', async ({ page }) => {
  await page.goto('/#/material/actions');
  for (const variant of ['Filled', 'Elevated', 'FilledTonal', 'Outlined', 'Text']) {
    const button = page.getByRole('button', { name: `Disabled ${variant}`, exact: true });
    await expect(button).toBeDisabled();
    await expect(button).toHaveCSS('opacity', '1');
    const colors = await button.evaluate(element => {
      const style = getComputedStyle(element);
      return { background: style.backgroundColor, foreground: style.color, shadow: style.boxShadow };
    });
    expect(colors.foreground).toMatch(/0\.38[0-9]*\)$/);
    if (['Filled', 'Elevated', 'FilledTonal'].includes(variant)) {
      expect(colors.background).toMatch(/0\.12[0-9]*\)$/);
    } else {
      expect(colors.background).toBe('rgba(0, 0, 0, 0)');
    }
    if (variant === 'Elevated') expect(colors.shadow).toBe('none');
  }
});

test('selection descriptions stay beside the control and trailing controls reach the row edge', async ({ page }) => {
  await page.goto('/#/material/selection');
  const checkbox = page.getByRole('checkbox', { name: 'Receive updates', exact: true });
  const description = page.getByText('Optional product news', { exact: true });
  await expect(checkbox).toHaveAccessibleDescription('Optional product news');
  const copy = description.locator('..');
  const inputBox = (await checkbox.boundingBox())!;
  const copyBox = (await copy.boundingBox())!;
  expect(copyBox.x).toBeGreaterThan(inputBox.x + inputBox.width);
  expect(inputBox.y + inputBox.height / 2).toBeGreaterThanOrEqual(copyBox.y);
  expect(inputBox.y + inputBox.height / 2).toBeLessThanOrEqual(copyBox.y + copyBox.height);
  const trailing = page.getByRole('checkbox', { name: 'Trailing control', exact: true });
  const rowBox = (await trailing.locator('..').boundingBox())!;
  const controlBox = (await trailing.boundingBox())!;
  const parentBox = (await trailing.locator('../..').boundingBox())!;
  expect(rowBox.width).toBeGreaterThan(300);
  expect(Math.abs(rowBox.width - parentBox.width)).toBeLessThan(2);
  await expect(trailing).toHaveCSS('margin-inline-end', '2px');
  expect(Math.abs(rowBox.x + rowBox.width - controlBox.x - controlBox.width - 2)).toBeLessThan(2);
});

test('explicit disabled selection colors are not faded or ignored', async ({ page }) => {
  await page.goto('/#/material/selection');
  for (const [role, name] of [
    ['checkbox', 'Disabled custom checkbox'],
    ['switch', 'Disabled custom switch'],
    ['radio', 'Disabled custom radio'],
  ] as const) {
    const control = page.getByRole(role, { name, exact: true });
    await expect(control).toBeDisabled();
    await expect(control).toHaveCSS('opacity', '1');
    await expect(control).toHaveCSS('border-top-color', 'rgb(148, 103, 73)');
    if (role === 'switch') {
      await expect(control).toHaveCSS('background-color', 'rgb(243, 232, 222)');
      await expect(control).toHaveCSS('background-image', /rgb\(64, 45, 33\)/);
    }
  }
});

test('selection label and description styles configure color and spacing independently', async ({ page }) => {
  await page.goto('/#/material/selection');
  const control = page.getByRole('checkbox', { name: 'Disabled custom checkbox', exact: true });
  const label = page.getByText('Disabled custom checkbox', { exact: true });
  const description = page.getByText('Configured disabled description', { exact: true });
  await expect(control).toHaveAccessibleDescription('Configured disabled description');
  await expect(label).toHaveCSS('color', 'rgb(148, 103, 73)');
  await expect(description).toHaveCSS('color', 'rgb(64, 45, 33)');
  await expect(description.locator('..')).toHaveCSS('gap', '4px');
});

for (const theme of ['light', 'dark', 'brand-light', 'brand-dark']) {
  test(`selection geometry and disabled colors in ${theme}`, async ({ page, browserName }) => {
    await page.goto(`/#/material/compare/${theme}/states`);
    const checked = page.getByRole('checkbox', { name: 'Enabled selected checkbox', exact: true });
    await expect(checked).toBeChecked();
    await expect(checked).toHaveCSS('width', '20px');
    await expect(checked).toHaveCSS('height', '20px');
    expect(await checked.evaluate(element => getComputedStyle(element, '::after').maskImage)).toContain('M4');
    const disabled = page.getByRole('checkbox', { name: 'Disabled selected checkbox', exact: true });
    await expect(disabled).toBeDisabled();
    await expect(disabled).toBeChecked();
    const slider = page.getByRole('slider', { name: 'Slider 0.5', exact: true }).first();
    await expect(slider).toHaveCSS('height', '48px');
    if (browserName === 'chromium') {
      // Chromium does not expose thumb paint geometry through getComputedStyle; the direct renderer report captures it.
      await expect(slider).toHaveCSS('appearance', 'none');
    }
    await slider.focus();
    await expect(slider).toBeFocused();
    const disabledSlider = page.getByRole('slider', { name: 'Slider 0.5', exact: true }).last();
    await expect(disabledSlider).toBeDisabled();
    expect(await disabledSlider.evaluate(element => getComputedStyle(element).getPropertyValue('--d-slider-thumb-current')))
      .toContain('38%');
  });
}


test('app-owned control labels do not leave invisible layout gaps', async ({ page }) => {
  await page.goto('/#/material/compare/light/states');
  for (const [role, name] of [
    ['checkbox', 'Enabled unselected checkbox'],
    ['switch', 'Enabled unselected switch'],
    ['radio', 'Enabled unselected radio'],
  ] as const) {
    const control = page.getByRole(role, { name, exact: true });
    const visual = (await control.boundingBox())!;
    const target = (await control.locator('..').boundingBox())!;
    expect(target.height).toBe(48);
    expect(target.width).toBeGreaterThanOrEqual(48);
    expect(Math.abs(visual.x + visual.width / 2 - target.x - target.width / 2)).toBeLessThan(1);
    expect(Math.abs(visual.y + visual.height / 2 - target.y - target.height / 2)).toBeLessThan(1);
  }
});


test('stepped sliders paint markers and continuous sliders retain their end stop', async ({ page }) => {
  await page.goto('/#/material/compare/light/states');
  for (const name of ['Stepped slider', 'Slider 0.5']) {
    const slider = page.getByRole('slider', { name, exact: true }).first();
    const screenshot = await slider.screenshot({ scale: 'css' });
    await test.info().attach(name, { body: screenshot, contentType: 'image/png' });
    // Inspect actual paint: Chromium and WebKit do not expose native track pseudo styles.
    const samples = await page.evaluate(async ({ image, stepped }) => {
      const bitmap = new Image();
      bitmap.src = `data:image/png;base64,${image}`;
      await bitmap.decode();
      const canvas = document.createElement('canvas');
      canvas.width = bitmap.width;
      canvas.height = bitmap.height;
      const context = canvas.getContext('2d')!;
      context.drawImage(bitmap, 0, 0);
      const fractions = stepped ? [0, 0.25, 0.75, 1] : [1];
      return fractions.map(fraction => {
        const x = Math.round(10 + (bitmap.width - 20) * fraction);
        const y = Math.floor(bitmap.height / 2);
        return {
          marker: Array.from(context.getImageData(x, y, 1, 1).data).slice(0, 3),
          track: Array.from(context.getImageData(x - (fraction === 0 ? -4 : 4), y, 1, 1).data).slice(0, 3),
          active: fraction < 0.5,
        };
      });
    }, { image: screenshot.toString('base64'), stepped: name === 'Stepped slider' });
    for (const sample of samples) {
      const primary = [87, 70, 178];
      const container = [232, 222, 248];
      for (const [actual, expected] of [
        [sample.marker, sample.active ? container : primary],
        [sample.track, sample.active ? primary : container],
      ]) {
        actual.forEach((channel, index) => expect(Math.abs(channel - expected[index])).toBeLessThanOrEqual(12));
      }
    }
  }
});

for (const name of ['Enabled unselected checkbox', 'Enabled unselected switch', 'Enabled unselected radio']) {
  test(`selection feedback paints hover, press and keyboard focus for ${name}`, async ({ page }) => {
    await page.goto('/#/material/compare/light/states');
    const control = page.getByLabel(name, { exact: true });
    const row = control.locator('..');
    await control.hover();
    expect(await row.evaluate(element => getComputedStyle(element, '::before').opacity)).toBe('0.08');
    await page.mouse.down();
    expect(await row.evaluate(element => getComputedStyle(element, '::before').opacity)).toBe('0.1');
    await page.mouse.up();
    await page.mouse.move(0, 0);
    await control.focus();
    await page.keyboard.press('Tab');
    await page.keyboard.press('Shift+Tab');
    await expect(control).toBeFocused();
    expect(await control.evaluate(element => element.matches(':focus-visible'))).toBe(true);
    expect(await row.evaluate(element => getComputedStyle(element, '::before').opacity)).toBe('0.1');
    await expect(row).toHaveCSS('outline-width', '3px');
    await expect(row).toHaveCSS('outline-style', 'solid');
  });
}


test('Material defaults keep component surfaces compact and preserve explicit theme overrides', async ({ page }) => {
  await page.goto('/#/material/compare/light/actions');
  const filled = page.getByRole('button', { name: 'Filled', exact: true });
  await expect(filled).toHaveCSS('height', '40px');
  const outlined = page.getByRole('button', { name: 'Outlined', exact: true });
  const outline = await outlined.evaluate(element => ({
    color: getComputedStyle(element).color,
    token: (() => {
      const color = document.createElement('span').style;
      color.color = getComputedStyle(element.closest('.d-theme')!).getPropertyValue('--d-on-surface-variant').trim();
      return color.color;
    })(),
  }));
  expect(outline.color).toBe(outline.token);
  const extended = page.getByRole('button', { name: 'Compose', exact: true });
  await expect(extended).toContainText('Compose');
  await page.goto('/#/material/compare/light/selection');
  const chip = page.locator('.d-chip-assist').first();
  await expect(chip).toHaveCSS('height', '32px');
  await expect(chip).toHaveCSS('background-color', 'rgba(0, 0, 0, 0)');
  await page.goto('/#/material/compare/light/surfaces');
  const card = page.locator('.d-card').filter({ has: page.getByText('Filled card', { exact: true }) });
  await expect(card).toHaveCSS('padding', '0px');
  await page.goto('/#/material/selection');
  await page.getByRole('switch', { name: 'Component overrides', exact: true }).check();
  const segment = page.getByRole('radiogroup', { name: 'Period', exact: true }).locator('.d-segment-surface').first();
  await expect(segment).toHaveCSS('min-height', '40px');
  await expect(segment).toHaveCSS('padding', '8px 12px');
});


test('rails retain labels while closed drawers do not duplicate their content', async ({ page }) => {
  await page.goto('/#/material/compare/light/navigation');
  const rail = page.locator('.d-wide-navigation-rail');
  const home = rail.getByRole('button', { name: 'Home', exact: true });
  await expect(home.locator('.d-rail-label')).toBeVisible();
  const saved = rail.getByRole('button', { name: 'Saved', exact: true });
  const first = (await home.boundingBox())!;
  const next = (await saved.boundingBox())!;
  expect(next.y).toBeGreaterThanOrEqual(first.y + first.height);
  await expect(page.locator('.d-navigation-drawer')).toHaveCount(0);
  await page.getByRole('button', { name: 'Open navigation drawer', exact: true }).click();
  const drawer = page.locator('.d-navigation-drawer');
  await expect(drawer).toBeVisible();
  await expect(drawer.getByRole('button', { name: 'Home', exact: true })).toHaveCount(1);
});
