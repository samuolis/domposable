# Before the first public release

## Release setup still pending

- Public demo downloads contain the signed Android APK. Apple signing and public iOS downloads are deferred; iOS library compilation and simulator tests remain in CI.
- Review and merge the expanded demo/visual-check PR, then deploy the updated bootstrap site through its approved workflow.
- Run the first **Bump and release** action, verify Maven Central publication and signed downloads, and install the Android demo on a real device. The checked-in `alpha00` is still unpublished.
- Keep dedicated credential backups secure and track publishing-token, certificate and developer-membership expiry dates privately.

## Product follow-ups

- Decide which APIs to stabilize after using the library in a real feature. Gather migration feedback from a team using its own DI/state architecture.
- Add Wasm URL-history parity and deeper web accessibility/input interaction coverage. It currently has an initial-route fixture and in-memory navigation.
- Test a complete Kobweb application/export, beyond the compiled page wrapper, before claiming full Kobweb-version support.
- Evaluate independent root back stacks, richer Navigation 3 scene strategies, HTML list virtualization and large-data performance.
- Manually review native assistive technology, browser screen readers, keyboard/touch behavior, dark themes and real-device rendering.

These are explicit remaining boundaries, not prerequisites for every application that wants to try an alpha. Additional platforms should be added through renderer/source-set implementations and published-consumer checks rather than a larger mandatory screen abstraction.

Public demo downloads currently include Android APKs. iOS download/signing configuration is deferred; iOS library compilation and simulator regression checks remain required.
