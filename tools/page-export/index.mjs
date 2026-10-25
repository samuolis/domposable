import {createHash} from 'node:crypto';
import {mkdir, mkdtemp, readFile, rename, rm, writeFile} from 'node:fs/promises';
import {dirname, join, resolve} from 'node:path';

const manifestName = 'domposable-export.json';

/** Export paths are canonical public paths, never viewer-specific URLs or filesystem paths. */
export function pageFile(path) {
  if (typeof path !== 'string' || !path.startsWith('/') || path.startsWith('//') ||
      /[?#\\\x00-\x20]/.test(path)) throw new Error(`Invalid export path: ${path}`);
  const decoded = decodeURIComponent(path);
  if (decoded !== path && /[\/\\?#\x00-\x20]/.test(decoded.replaceAll('/', ''))) {
    throw new Error(`Encoded export separator: ${path}`);
  }
  if (/%2f|%5c/i.test(path) || decoded.split('/').some(part => part === '.' || part === '..') ||
      new URL(path, 'https://export.invalid').pathname !== path) throw new Error(`Unsafe export path: ${path}`);
  return join(decoded.slice(1), 'index.html');
}

/**
 * Reuse the application's browser renderer. The host owns routes, data access and readiness.
 * No credentials or storage state are copied from the developer's browser.
 */
export async function exportPages({browser, baseUrl, canonicalOrigin, outputDir, pages,
  root = '#root', markerAttribute = 'data-domposable-prerender', ready, prepareContext, settled}) {
  const source = new URL(baseUrl);
  const canonical = new URL(canonicalOrigin);
  if (!['http:', 'https:'].includes(source.protocol) || source.username || source.password ||
      canonical.protocol !== 'https:' || canonical.pathname !== '/' || canonical.search || canonical.hash) {
    throw new Error('Export requires an HTTP source and an HTTPS canonical origin');
  }
  if (!pages.length) throw new Error('Refusing an empty public-page export');
  if (!/^data-[a-z][a-z0-9-]*$/.test(markerAttribute)) throw new Error('Export marker must be a data attribute');
  const paths = new Set();
  const files = new Set();
  for (const page of pages) {
    const file = pageFile(page.path);
    if (files.has(file)) throw new Error(`Duplicate export path: ${page.path}`);
    paths.add(page.path);
    files.add(file);
  }
  const output = resolve(outputDir);
  await mkdir(output, {recursive:true});
  const staging = await mkdtemp(join(dirname(output), '.domposable-export-'));
  const exported = [];
  try {
    for (const entry of pages) {
      const context = await browser.newContext({locale:entry.locale ?? 'en-GB',
        viewport:{width:1280, height:900}, colorScheme:'light', serviceWorkers:'block'});
      try {
        await prepareContext?.(context, entry);
        const page = await context.newPage();
        const errors = [];
        page.on('pageerror', error => errors.push(error.message));
        const response = await page.goto(new URL(entry.path, source).href, {waitUntil:'domcontentloaded'});
        if (response?.status() !== 200 || /noindex/i.test(response.headers()['x-robots-tag'] ?? '') ||
            new URL(page.url()).pathname !== entry.path) {
          throw new Error(`Export did not reach the public page: ${entry.path}`);
        }
        await page.locator(root).waitFor();
        await ready?.(page, entry);
        await settled?.(page, entry);
        const snapshot = await page.evaluate(async ({root, origin, path, sourceOrigin, markerAttribute}) => {
          await document.fonts.ready;
          // Remote avatars are not export readiness; hosts can enforce additional media in settled().
          const ownedImages = [...document.images].filter(image =>
            new URL(image.currentSrc || image.src, location.href).origin === location.origin);
          await Promise.all(ownedImages.map(image => image.decode()));
          await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)));
          const host = document.querySelector(root);
          const title = document.title.trim();
          const description = document.querySelector('meta[name="description"]')?.content.trim();
          const canonical = [...document.querySelectorAll('link[rel="canonical"]')];
          if (!title || !description || !host?.querySelector('h1')?.textContent.trim() ||
              canonical.length !== 1 || canonical[0].href !== origin + path ||
              /noindex/i.test(document.querySelector('meta[name="robots"]')?.content ?? '')) {
            throw new Error(`Public content/metadata is incomplete: ${path}`);
          }
          for (const style of document.querySelectorAll('style')) {
            if (style.sheet) style.textContent = [...style.sheet.cssRules].map(rule => rule.cssText).join('\n').replaceAll(sourceOrigin + '/', '/');
          }
          for (const element of document.querySelectorAll('[src],[href],[action],[style]')) {
            for (const attribute of ['src','href','action','style']) {
              if (attribute === 'href' && element.matches('link[rel="canonical"],link[hreflang]')) continue;
              const value = element.getAttribute(attribute);
              if (value?.includes(sourceOrigin + '/')) element.setAttribute(attribute,value.replaceAll(sourceOrigin + '/', '/'));
            }
          }
          // Listeners cannot be serialized; links work immediately, controls wait for app startup.
          for (const control of host.querySelectorAll('button,input,select,textarea,[role="button"]:not(a)')) control.inert = true;
          host.setAttribute(markerAttribute, 'true');
          return {title, html:'<!doctype html>\n' + document.documentElement.outerHTML};
        }, {root, origin:canonical.origin, path:entry.path, sourceOrigin:source.origin, markerAttribute});
        if (errors.length) throw new Error(`Browser errors on ${entry.path}: ${errors.join('; ')}`);
        const file = join(staging, pageFile(entry.path));
        await mkdir(dirname(file), {recursive:true});
        await writeFile(file, snapshot.html);
        exported.push({path:entry.path, title:snapshot.title,
          sha256:createHash('sha256').update(snapshot.html).digest('hex'), bytes:Buffer.byteLength(snapshot.html),
          ...(entry.revision ? {revision:entry.revision} : {})});
      } catch (error) {
        throw new Error(`Export failed for ${entry.path}: ${error.message}`, {cause:error});
      } finally { await context.close(); }
    }
    let previous;
    try { previous = JSON.parse(await readFile(join(output, manifestName), 'utf8')); }
    catch (error) { if (error.code !== 'ENOENT') throw error; }
    for (const entry of previous?.pages ?? []) pageFile(entry.path);
    // Publish only after every page validated. CI must stop on any export/publish failure.
    for (const entry of exported) {
      const target = join(output, pageFile(entry.path));
      await mkdir(dirname(target), {recursive:true});
      await rename(join(staging, pageFile(entry.path)), target);
    }
    for (const entry of previous?.pages ?? []) {
      if (!paths.has(entry.path)) await rm(join(output, pageFile(entry.path)), {force:true});
    }
    const manifest = {version:1, generatedAt:new Date().toISOString(), pages:exported};
    await writeFile(join(staging, manifestName), JSON.stringify(manifest, null, 2) + '\n');
    await rename(join(staging, manifestName), join(output, manifestName));
    return manifest;
  } finally { await rm(staging, {recursive:true, force:true}); }
}
