# Architecture

The UI module depends on foundation and includes the built-in platform renderers.
Adaptive layout and screen ownership are separate modules. Navigation and lifecycle
build on screen ownership. KSP operates at build time.

Shared features call portable components with props, callbacks and
`DomposableModifier`. Each built-in component delegates directly to an internal
`expect` function. Kotlin selects its `actual` implementation at compile time:
Compose UI and Material controls on native targets, semantic HTML on Kotlin/JS.
The Compose runtime handles composition and state on both paths.

There is no built-in component provider registry. Theme composition locals carry
colors, typography, shapes and component styles. Applications can keep their own
component names, wrap library controls, or implement an app-owned `expect` / `actual`
control when its structure or behavior differs. The optional typed extension API
remains available for applications that explicitly need runtime replacement of
custom components. It is not part of built-in dispatch.

`domposable-design-material` remains a compatibility dependency that exposes
`domposable-ui`. Existing imports of Material theme APIs stay valid. New consumers
can depend on `domposable-ui` directly. Native Material dependencies now belong to
that module. Browser consumers use the HTML implementation.

Internal KSP generates props forwarding functions when the library is built.
Applications need no component code generation. Direct calls let Kotlin/JS dead
code elimination remove unused controls without retaining a catalog-wide registry.

Screen signatures and model creation remain application-owned. A screen can take
a plain model, scalar values and callbacks, a state machine, an AndroidX ViewModel,
or local state. Component rendering does not require a DI framework.

Entry resources survive adaptive pane changes. Removing an entry destroys its
resources once. Browser history preserves saveable UI data and recreates disposed
entries. It never keeps removed ViewModels alive indefinitely.

DOM APIs, Compose UI interop and native controls stay in platform source sets.
Native Compose UI nodes cannot run under the DOM applier.
