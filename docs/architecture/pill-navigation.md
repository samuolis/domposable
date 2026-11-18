# Floating pill navigation adapter

`DomposablePillNavigationBar` declares keyed destinations and delegates its Compose renderer to the independent Compose Pill library. Wrap a page in `DomposablePillBackdropHost`, and place `DomposablePillBackdropSource` around content behind the bar. The navigation item callbacks and application routes remain caller-owned. See `consumer-fixture/src/commonMain/kotlin/example/consumer/PillConsumer.kt` for a portable example.

Compose targets support solid, frosted and custom glass appearances. A missing source uses the library's solid fallback. Semantic HTML renders an ordinary accessible navigation element and does not promise the Compose visual effect, collapse animation or scrubbing. HTML retains safe destination links, including modified clicks, and exposes the supplied accessible name and badge.

The dependency is limited to `composeUiMain`; no Compose nodes run under the DOM applier. The initial adapter depends on Compose Pill 0.1.0 and must not merge until that artifact is available to packaged consumers.
