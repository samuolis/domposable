# Domposable

**One Compose API for apps and semantic HTML.**

Domposable gives feature teams a small shared UI vocabulary: ordinary composable screens, ordered modifiers, component renderers, theme tokens and adaptive recipes. Android and iOS use Compose UI. Kotlin/JS uses HTML elements, CSS and native browser controls. JVM desktop is experimental.

```kotlin
@Composable
fun ContactsScreen(model: ContactsModel) {
    DomposableColumn(gap = 12.dp) {
        DomposableText("Contacts", style = DomposableTextStyle.Title)
        DomposableButton("Add contact", model::addContact)
    }
}
```

Your application supplies its models and dependencies. Plain Compose state, MVVM, MVI, Metro, Koin, Dagger and manual construction are application choices. Screens return `Unit`; neither state/action wrappers nor dependency injection are mandatory.

## Modules

| Artifact | Purpose |
| --- | --- |
| `domposable-foundation` | Portable values and theme tokens |
| `domposable-ui` | Shared components, renderer contracts and modifiers |
| `domposable-design-material` | Optional native Material and semantic HTML defaults |
| `domposable-adaptive` | Content-sized breakpoints, preferences and pane recipes |
| `domposable-screens` | Typed registration and entry-owned resources/saveables |
| `domposable-navigation3` | Optional Navigation 3 hosts and browser history |
| `domposable-lifecycle-viewmodel` | Optional AndroidX model/lifecycle ownership |
| `domposable-ksp` | Optional generated screen registration |

Coordinates: `io.github.samuolis.domposable:<artifact>:<released version>`. The checked-in `0.1.0-alpha00` is an unpublished bootstrap version. The first release action produces `0.1.0-alpha01`.

[Guides](https://samuolis.github.io/domposable/) · [Demo](https://samuolis.github.io/domposable/demo/) · [Release downloads](https://github.com/samuolis/domposable/releases)

## Development

Use JDK 21, Node 24, Android SDK 37.2 and macOS/Xcode 26 for iOS. Put your SDK path in ignored `local.properties`, or set `ANDROID_HOME`.

```sh
./gradlew jvmTest :androidApp:assembleDebug :demo:jsBrowserDevelopmentWebpack
npm ci
npx playwright install chromium firefox webkit
python3 scripts/ci/browser_preview.py
npx playwright test
```

For iOS, build `:demo:linkDebugFrameworkIosSimulatorArm64`, then run `python3 scripts/ci/ios_test.py`. CI checks API baselines and consumers resolving actual published variants; see [CONTRIBUTING.md](CONTRIBUTING.md).

The **Bump and release** GitHub Action owns `version.properties`, publishes Maven artifacts, attaches a signed APK, Ad Hoc IPA, simulator app and symbols, and deploys the website to GitHub Pages. [Release setup and recovery](docs/releasing.md).

Alpha APIs may change. HTML collections currently render keyed DOM lists rather than virtualizing; platform escape hatches remain platform-specific. See [coverage](docs/coverage.md) for current boundaries.

Apache-2.0. Copyright 2026 Samuolis.
