# Release setup and recovery

The single **Bump and release** action reads `version.properties`. Choose prerelease/channel, patch, minor, major or stable. Normal operation needs one dispatch. Alpha sequence `00` is the unpublished baseline; the first dispatch increments it to `01` and build number 1. Stable promotes the current prerelease; patch/minor/major start a stable version at a new core number.

The action creates a temporary version-only commit, waits for that exact commit's CI, builds signed downloads and a signed Maven bundle, stages immutable assets in a draft, validates Central, fast-forwards `main`, publishes Central, verifies consumers, exposes the GitHub release and deploys its preserved website. Implementation changes use PRs. The release app's direct version push was explicitly approved by the owner.

## Repository configuration

1. Enable GitHub Actions and select **GitHub Actions** as the Pages source.
2. Create a `release` environment restricted to the `main` branch. The Pages deployment uses this environment too. A reviewer is optional; adding one adds a manual approval to releases.
3. Create a dedicated GitHub App, install it on this repository only, grant Contents read/write and Actions read, and store its ID in `RELEASE_APP_ID` and private key in `RELEASE_APP_PRIVATE_KEY`.
4. Protect `main` with two rulesets. One requires PRs and permits the release app to bypass that PR rule. The second requires **CI passed**, blocks force pushes/deletion and grants the app no bypass. Do not give the app a blanket exemption from CI. Require checks on the candidate commit; configure the CI Actions integration as the expected source if available.
5. Verify ownership of `io.github.samuolis` in Sonatype Central. Create a portal token and configure `CENTRAL_USERNAME` / `CENTRAL_PASSWORD`. Publish the public GPG key to a supported keyserver and store the ASCII-armored private key/password in `MAVEN_SIGNING_KEY` / `MAVEN_SIGNING_PASSWORD`.
6. Create dedicated demo signing identities. Store secrets and variables below in the release environment; never reuse another application's production credentials.

| Kind | Name | Value |
| --- | --- | --- |
| Secret | `ANDROID_KEYSTORE_BASE64` | Base64 of dedicated release keystore |
| Secret | `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| Secret | `ANDROID_KEY_ALIAS` | Signing alias |
| Secret | `ANDROID_KEY_PASSWORD` | Key password |
| Variable | `ANDROID_CERT_SHA256` | Dedicated certificate SHA-256, colon separators allowed |
| Secret | `IOS_CERTIFICATE_BASE64` | Base64 Apple Distribution p12 |
| Secret | `IOS_CERTIFICATE_PASSWORD` | p12 password |
| Secret | `IOS_PROFILE_BASE64` | Base64 Ad Hoc profile for `io.github.samuolis.domposable.demo` |
| Variable | `IOS_TEAM_ID` | Apple developer team ID |

Ad Hoc IPA installation works on devices registered in that profile. The arm64 simulator app needs no registered device. The script checks profile type, expiry, application/team IDs, certificate matching, code signatures and version values; it creates and deletes a temporary keychain and its dedicated profile copy. Credentials are never release assets.

## Retry behavior

Dispatch again after fixing configuration or transient service failure. An unfinished release takes precedence over the new bump request. The action restores its original commit/assets and Central deployment ID. It does not manufacture a new version or rebuild already staged binaries. Draft assets and `manifest.json` retain SHA-256 checksums; `release-state.json` identifies progress. Interrupted draft uploads recover the same immutable Actions artifact, retained for 90 days. After draft upload completes, GitHub release assets preserve the bundle permanently.

If a Central upload response is lost before its ID is saved, the action stops. Locate the deployment named `domposable-<version>-<bundle hash>` in Central and supply its ID through the recovery input. The action never blindly uploads a duplicate. Central failures remain reserved until the cause is understood; Maven coordinates cannot be overwritten.

If `main` changes while a new candidate is building, the version push fails safely. Reconcile the unpublished draft candidate before starting a new release; do not delete a draft whose Central deployment is publishing or published. Later implementation commits are never rolled back. Once publication starts, finish that same release through its saved checkpoint.

Pages always receives the `website.zip` attached to that release. A Pages-only failure resumes the same site deployment. Tags, published artifacts and public downloads refer to the tested version commit.

A local build can exercise all unsigned artifacts and scripts, but live signing, App ruleset behavior, Central publication and Pages deployment require the repository configuration above. CI and release workflow files alone cannot create Apple identities or verify a Central namespace.
