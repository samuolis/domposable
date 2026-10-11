# Domposable

One Compose API for apps and semantic HTML.

Write a feature once using ordinary `@Composable` functions. Supply your own models. Choose a component provider to render it with Compose UI on Android/iOS or semantic HTML in a browser.

Domposable keeps feature contracts independent of dependency injection and state architecture. It adds portable UI primitives, ordered modifiers, semantic tokens, measured adaptive layouts and optional screen/navigation ownership.

Try the [task workspace and component catalog](https://samuolis.github.io/domposable/demo/) and download [native demos](https://github.com/samuolis/domposable/releases). The website and downloads identify their commit in `release.json`.

Until the first Maven release, the site is a tested bootstrap preview at `0.1.0-alpha00`. That version is not available on Maven Central. Native debug downloads are available in the successful [CI run artifacts](https://github.com/samuolis/domposable/actions/workflows/ci.yml). Published releases replace this preview with the website attached to the release.

Android, iOS arm64 and Kotlin/JS are the initial targets. JVM desktop is experimental. Alpha coverage and limitations are listed in [Coverage](coverage.md).
