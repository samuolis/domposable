# Public page export

Export existing Compose HTML screens as HTML/CSS at build time. The supported
consumer setup is a Kotlin Gradle `ExportSite` task and a `public-pages.txt` file;
no user-written exporter JavaScript or npm project is required.

See the [complete setup and limitations](https://github.com/samuolis/domposable/blob/main/docs/page-export.md).
The Gradle artifact is planned for the next minor release and is not yet published.

## Advanced Node integrations

Existing Node hosts can install an immutable source archive of this package and a
compatible Playwright development dependency, then run:

```js
import {exportSite} from '@samuolis/domposable-page-export';

await exportSite({
  directory:'build/site',
  canonicalOrigin:'https://example.com',
  pages:['/about/', '/events/summer-cup'],
});
```

Install Chromium on the build machine with `npx playwright install chromium --only-shell`.
`exportSite` owns the temporary server/browser and supports host readiness/data-policy
hooks. `exportPages` remains available for hosts with an existing server/browser.
See the full guide for anonymous context isolation, metadata checks, snapshot startup,
responsive CSS and dynamic-data freshness requirements.
