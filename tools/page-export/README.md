# Public page export

Build-time prerendering of existing Compose HTML pages. This is a Node build tool,
not a Kotlin runtime dependency, an SSR server or a hydration implementation.
The host supplies a Playwright browser, explicit public paths, data policy and readiness.
It renders the same screens used by the application, serializes generated CSS, validates
content/metadata and writes HTML plus a content-hashed `domposable-export.json` inventory.

```js
import {chromium} from '@playwright/test';
import {exportPages} from '@samuolis/domposable-page-export';

const browser = await chromium.launch();
try {
  await exportPages({
    browser,
    baseUrl:'http://127.0.0.1:4173', // host serves the production app shell
    canonicalOrigin:'https://example.com',
    outputDir:'build/site',
    pages:[{path:'/about/'}, {path:'/events/summer-cup', revision:'public-data-hash'}],
    ready:async page => {
      await page.locator('main h1').waitFor();
      await page.waitForFunction(() => !document.querySelector('main [aria-busy="true"]'));
    },
  });
} finally { await browser.close(); }
```

Each page gets a fresh anonymous context. Export only routes whose data is public;
never pass share tokens or viewer sessions. The optional `prepareContext(context, entry)`
hook lets the host select anonymous API endpoints; `settled(page, entry)` can enforce its
existing resource-readiness checks. These hooks must not install authenticated state.
Same-origin images must decode. Remote avatars do not block export; use `settled` if
the host requires particular remote artwork to be ready.
Failed routes, redirects, browser errors, missing content/description/canonical metadata
and `noindex` pages abort publication. Previous owned pages absent from a successful new
inventory are removed; a failed capture leaves the previous export untouched.

The HTML root has `data-domposable-prerender="true"` (or the host's existing
`markerAttribute`). The browser application must
replace that root when starting, as regular Compose HTML does not hydrate it. Snapshot
buttons/form controls are inert; anchors remain usable. Scripts remain in the HTML.
Exporting does not itself reduce the application's JavaScript payload.

Responsive layouts must be expressed in CSS, and appearance must be valid before app
startup. Exporting JavaScript-measured inline layout freezes the export viewport. Test
the resulting files with JavaScript disabled at phone/desktop widths and light/dark
appearance; then test the live controls with JavaScript enabled.

For changing public data, the host owns discovery, refresh, deletion and visibility
checks. `revision` is opaque host metadata, not an automatic freshness guarantee.
Do not serve a stored event snapshot after it becomes private or when its revision is
outdated. Never run a headless browser on each visitor request.
