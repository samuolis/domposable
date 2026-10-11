# Architecture

The dependency direction is `foundation → ui → optional design provider`. Adaptive and screen ownership are separate modules. Navigation and lifecycle build on screen ownership, and KSP operates at build time.

Shared features depend on portable props and `DomposableModifier`. Platform providers translate props into Compose UI or DOM elements. The Compose runtime handles composition/state on both paths; the browser renderer emits semantic HTML rather than a canvas.

Each component has one props declaration and a generated renderer SAM interface. The repository's internal KSP mode produces forwarding functions and the typed `DomposableComponents` provider. Consumers use the published contracts; their builds need no component code generation.

Screen signatures are application-owned. A screen can receive a plain model, scalar values with callbacks, a state machine, an AndroidX ViewModel, or use local state. Registration only needs a route type and composable renderer. Models are created at application boundaries, never implicitly injected by the library.

Entry resources survive adaptive pane changes. Removing an entry destroys its resources once. Browser history preserves saveable UI data and recreates disposed entries; it never keeps removed ViewModels alive indefinitely.

Future targets can implement the same renderer interfaces and modifier translation. DOM-specific APIs, Compose UI interop and native controls live in target source sets. Platform differences remain explicit when equivalent behavior cannot be implemented.
