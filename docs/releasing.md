# Release setup and recovery

The single **Bump and release** action reads `version.properties`, which contains only `VERSION=MAJOR.MINOR.PATCH`. Choose **patch**, **minor** or **major**; published versions have no suffix or separately managed build number. Normal operation needs one dispatch. The unpublished bootstrap is `0.0.0`; choose **minor** for the first `0.1.0` release.

The Android demo derives its installation `versionCode` using Ranker's existing encoding: `major × 100,000 + minor × 100 + patch`. Version `0.4.0` produces `400`, and `0.4.1` produces `401`. Minor must stay below 1000 and patch below 100, and the result must not exceed Android's 2,100,000,000 limit; invalid versions fail before a release candidate is pushed. The iOS demo uses the same SemVer string for its marketing and build versions. Release metadata records the derived Android code for artifact verification, but library versions, release names and downloads use SemVer only. Historical checkpoints retain their original installation codes when recovering already-built immutable artifacts.

The action creates a temporary version-only commit, waits for that exact commit's CI, builds a signed Android APK and a signed Maven bundle, stages immutable assets in a draft, validates Central, fast-forwards `main`, publishes Central, verifies consumers, exposes the GitHub release and deploys its preserved website. Implementation changes use PRs. Obtain the owner's approval for the specific version-only `main` push before dispatching a release.

## Repository configuration

1. Enable GitHub Actions and select **GitHub Actions** as the Pages source.
2. Create a `release` environment restricted to the `main` branch. The Pages deployment uses this environment too. A reviewer is optional; adding one adds a manual approval to releases.
3. Create a dedicated GitHub App, install it on this repository only, grant Contents read/write and Actions read, and store its ID in `RELEASE_APP_ID` and private key in `RELEASE_APP_PRIVATE_KEY`.
4. Protect `main` with two rulesets. One requires PRs and permits the release app to bypass that PR rule. The second requires **CI passed**, blocks force pushes/deletion and grants the app no bypass. Do not give the app a blanket exemption from CI. Require checks on the candidate commit; configure the CI Actions integration as the expected source if available.
5. Verify ownership of `io.github.samuolis` in Sonatype Central. Create a portal token and configure `CENTRAL_USERNAME` / `CENTRAL_PASSWORD`. Publish the public GPG key to a supported keyserver and store the ASCII-armored private key/password in `MAVEN_SIGNING_KEY` / `MAVEN_SIGNING_PASSWORD`.
6. Create a dedicated Android demo signing identity. Store secrets and variables below in the release environment; never reuse another application's production credentials.

| Kind | Name | Value |
| --- | --- | --- |
| Secret | `ANDROID_KEYSTORE_BASE64` | Base64 of dedicated release keystore |
| Secret | `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| Secret | `ANDROID_KEY_ALIAS` | Signing alias |
| Secret | `ANDROID_KEY_PASSWORD` | Key password |
| Variable | `ANDROID_CERT_SHA256` | Dedicated certificate SHA-256, colon separators allowed |

Public demo downloads currently include the signed Android APK only. The release verifies its dedicated certificate fingerprint and both version values before attaching it. iOS remains supported by the library and validated with simulator tests and visual captures in CI, but device/simulator downloads and Apple signing are deferred. A release requires no Apple credentials.

## HTML and Wasm build hashes

Libraries and the APK use the same plain semantic version. The HTML and Wasm distributions also have independent SHA-256 build identifiers. Public demo URLs stay at `/demo/` and `/wasm/`; their HTML loads `demo.<full-content-sha256>.js`. Source maps and licence sidecars receive their own content hashes, while Wasm binaries retain webpack's content-hashed names. Nested task and shopping routes reference the same fingerprinted HTML bundle.

Each distribution contains `web-assets.json`, recording the entry filename and the SHA-256 of every packaged file. Its distribution hash covers the sorted file/checksum inventory, so a resource change also changes the build identifier. `release.json` links both build identifiers to the library version, derived Android code and Git commit:

```json
{
  "version": "0.4.0",
  "build": 400,
  "commit": "<git-commit>",
  "status": "release",
  "web": {
    "html": {"entry": "demo.<sha256>.js", "sha256": "<distribution-sha256>"},
    "wasm": {"entry": "demo.<sha256>.js", "sha256": "<distribution-sha256>"}
  }
}
```

CI runs browser tests against the assembled production website at the real `/domposable/` project base. It checks entry/binary hashes, nested route reloads, HTML rendering and Wasm startup. Bootstrap deployments verify both full manifests before deploying; release deployments preserve these exact distributions in `website.zip`.

## Retry behavior

Dispatch again after fixing configuration or transient service failure. An unfinished release takes precedence over the new bump request. The action restores its original commit/assets and Central deployment ID. It does not manufacture a new version or rebuild already staged binaries. Draft assets and `manifest.json` retain SHA-256 checksums; `release-state.json` identifies progress. Interrupted draft uploads recover the same immutable Actions artifact, retained for 90 days. After draft upload completes, GitHub release assets preserve the bundle permanently.

If a Central upload response is lost before its ID is saved, the action stops. Locate the deployment named `domposable-<version>-<bundle hash>` in Central and supply its ID through the recovery input. The action never blindly uploads a duplicate. Central failures remain reserved until the cause is understood; Maven coordinates cannot be overwritten.

For a rejected bundle that needs a packaging fix, restore its draft assets and `release-state.json` into `.release/artifacts/` and `.release/state.json`, configure the existing GitHub/Central credentials, and run `python scripts/release.py abandon` **before merging the fix**. This command requires a staged draft, a fresh Central `FAILED` response, unchanged main and no existing version tag. It retains the original signed assets and deployment record as a failed draft archive. Merge the tested fix, then dispatch **Bump and release** again; because no version commit advanced main, the corrected candidate uses the same unpublished version. Publishing or published deployments cannot use this recovery path.

Packaging verifies signatures for every publication attachment and writes MD5, SHA-1, SHA-256 and SHA-512 checksums for every payload and signature, including Kotlin tooling metadata JSON and Compose resource ZIP files. GitHub publication explicitly selects the tested commit and SemVer tag, even when GitHub assigns a temporary tag to a draft.

If `main` changes while a new candidate is building, the version push fails safely. Reconcile the unpublished draft candidate before starting a new release; do not delete a draft whose Central deployment is publishing or published. Later implementation commits are never rolled back. Once publication starts, finish that same release through its saved checkpoint.

Pages always receives the `website.zip` attached to that release. A Pages-only failure resumes the same site deployment. Tags, published artifacts and public downloads refer to the tested version commit.

A local build can exercise all unsigned artifacts and scripts, but live signing, App ruleset behavior, Central publication and Pages deployment require the repository configuration above. CI and release workflow files alone cannot create Apple identities or verify a Central namespace.

## Initial website preview

Before the first release, **Publish bootstrap website** deploys the complete guides, API reference and HTML demo from a successful CI run for the exact main commit. It is restricted to the unpublished `0.0.0` baseline and records `status: preview` in `release.json`. After a version is released, websites use the ordinary **Bump and release** pipeline.

The dedicated Maven public key is checked in as `signing-key.asc`; its primary fingerprint is `646AC0FE7F73C347929C825937E5A0EA1E6DA396`. It is also published to `keyserver.ubuntu.com` for Central signature verification. Private identities are held in the protected release environment, never in source control.
