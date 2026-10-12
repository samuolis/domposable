# Release readiness and product follow-ups

## Release validation

- Public demo downloads contain the signed Android APK. Apple signing and public iOS downloads are deferred; iOS library compilation and simulator tests remain in CI.
- Require platform behavior, visual comparisons, public ABI and published-consumer checks before merging a release input. Deploy the website from the same candidate as the Maven artifacts and APK.
- Use **Bump and release**, verify Maven Central coordinates and signed APK identity/version, and install the demo on a real Android device. `0.0.0` is reserved for the unpublished bootstrap baseline.
- Keep dedicated credential backups secure and track publishing-token, certificate and developer-membership expiry dates privately.

## Product follow-ups

- Continue pre-1.0 API iteration with plain semantic versions. There is no scheduled stable 1.0 release. Ranker adoption remains postponed; application feedback can guide later API decisions.
- Wasm now exercises Unicode editing, password masking, disabled/error state, modal isolation/dismissal and a bounded axe regression audit. Resolve the documented upstream semantics-bridge findings before claiming a clean accessibility scan; physical review remains deferred.
- The application-owned root-stack recipe demonstrates snapshot restoration and custom native Navigation 3 list/detail scenes. Extend it for product-specific overlays, predictive-back policy or live models retained across inactive roots when needed.
- Columns now support measured heights; horizontal rows, responsive grids and semantic tables have opt-in windows and browser regression checks. Column virtualization, remote paging and spreadsheet editing remain application/provider work.

Physical-device assistive-technology review is deferred. Automated semantics, browser accessibility, interaction and simulator checks remain in CI; they do not establish screen-reader parity.

These are explicit remaining boundaries, not prerequisites for every application that wants to use Domposable. Additional platforms should be added through renderer/source-set implementations and published-consumer checks rather than a larger mandatory screen abstraction.
