# Public page export

Build-time prerendering of existing Compose HTML pages through Kotlin Gradle and an
internal Node engine. This is build tooling, not an application runtime dependency,
an SSR server or a hydration implementation.
The host supplies a built application directory, explicit public paths and data policy.
It renders the same screens used by the application, serializes generated CSS, validates
content/metadata and writes HTML plus a content-hashed `domposable-export.json` inventory.

## Kotlin Gradle setup

Add the task artifact to the build script classpath and register an export task in the
web application's `build.gradle.kts`. This task requires version 0.22.0
or later. Check [Releases](https://github.com/samuolis/domposable/releases) for
published versions; the example uses the first release containing the task.

```kotlin
import io.github.samuolis.domposable.export.ExportSite

buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath("io.github.samuolis.domposable:domposable-export-gradle:0.22.0")
    }
}

tasks.register<ExportSite>("exportPublicPages") {
    dependsOn("jsBrowserDistribution")
    directory.set(layout.buildDirectory.dir("dist/js/productionExecutable"))
    canonicalOrigin.set("https://example.com")
    pagesFile.set(layout.projectDirectory.file("public-pages.txt"))
}
```

Create `public-pages.txt` next to the module's `build.gradle.kts`:

```text
# Marketing
/about/
/pricing/

# Public events
/events/summer-cup
```

Use one canonical public path per line. Blank lines and lines starting with `#`
are ignored; surrounding whitespace is trimmed. Query strings, share tokens,
fragments and filesystem traversal paths are rejected. A missing file or an empty
combined page list fails the task. Entries are never automatically discovered.

Run `./gradlew exportPublicPages`. Consumers write no exporter JavaScript, npm package
or server setup. The published task bundles the exporter and lockfile, installs its
pinned Playwright dependency and Chromium, then owns the temporary server/browser.
Node 22+ with npm and Java 21+ must be installed on the build machine; first setup
requires network access. The task runs every time because public data can change
without an application rebuild. The task also supports Gradle's configuration cache.

`pagesFile` is optional. For a small list, use `pages.addAll("/about/", "/pricing/")`
instead. `pages` is a Gradle `ListProperty<String>`, so public event paths can also
be supplied by a Kotlin provider. If both are set, their entries are combined; duplicate
output paths are rejected. The application still owns anonymous
route discovery and publication policy. This task does not discover private routes.

`outputDirectory` defaults to `directory`; set it for separately stored snapshots.
`rootSelector` defaults to `#root`, and `markerAttribute` defaults to
`data-domposable-prerender`. The exporter waits for a nonempty `h1` and no busy content
inside that root. Metadata must already be supplied by the application. The original
`index.html` is saved as `app-shell.html`, so subsequent exports start from the
clean application rather than a previous snapshot. Keep this file with the build.

## Advanced Node integrations

Hosts that already maintain a Node build pipeline or need application-specific browser
hooks can use the source-distributed package. Install an immutable source archive and
Playwright as development dependencies, then install Chromium with
`npx playwright install chromium --only-shell`.

```js
import {exportSite} from '@samuolis/domposable-page-export';

await exportSite({
  directory:'build/site',
  canonicalOrigin:'https://example.com',
  pages:['/about/', '/events/summer-cup'],
});
```

Dynamic entries may instead be objects containing `path`, `locale` and an opaque
`revision`. Advanced hosts with an existing server/browser can use
`exportPages({browser, baseUrl, canonicalOrigin, outputDir, pages, ...hooks})`;
its `pages` entries are objects.

Each page gets a fresh anonymous context. Export only routes whose data is public;
never pass share tokens or viewer sessions. The optional `prepareContext(context, entry)`
hook lets the host select anonymous API endpoints; `ready(page, entry)` overrides
the default content wait; `settled(page, entry)` can enforce its
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
