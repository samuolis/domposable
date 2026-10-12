# Physical-device and assistive-technology review

Automated semantics, axe, screenshot and simulator checks run in CI. This checklist records the hands-on review of the actual app and its platform behavior. **Physical-device results are pending until a reviewer submits them.** A passing CI run does not fill in this checklist.

## Prepare the app

- Android: install the [signed 0.2.0 / build 3 APK](https://github.com/samuolis/domposable/releases/download/v0.2.0/domposable-0.2.0.apk) on an API 24+ device. It comes from commit `9e47fe8a883c619ab28b904207f8fac7aa774ecc`. Enable [TalkBack](https://support.google.com/accessibility/android/answer/6007100).
- iOS: use a physical device running the demo from the same source commit. Public iOS downloads are deferred. The unsigned simulator artifact cannot be installed on a phone. Build the device framework, generate the project, then select your device and development signing team in Xcode:

```sh
./gradlew :demo:linkDebugFrameworkIosArm64
python3 scripts/ci/generate_xcode_project.py
open iosApp/DomposableDemo.xcodeproj
```

Choose the `DomposableDemo` scheme and run it on the connected device. Keep signing configuration local. Enable [VoiceOver](https://support.apple.com/guide/iphone/turn-on-and-practice-voiceover-iph3e2e415f/ios). A simulator review remains useful but must be recorded as a simulator review.

For web, open the [HTML playground](https://samuolis.github.io/domposable/demo/) and [Wasm comparison](https://samuolis.github.io/domposable/wasm/) with your browser/screen-reader combination. Record each renderer separately. Canvas startup and screenshots do not establish Wasm screen-reader parity.

Record the version/build or source commit, device model, OS, browser if applicable, reader/version, font size and theme. Record **Pass**, **Fail** or **Not run** for each row; include reproduction steps for failures and a reason for checks not run.

## Review checklist

Start in Tasks, then open **Components → More components** and **Shop demo**. Use the screen reader without relying on sight; repeat the key actions with touch and an external keyboard where available.

| ID | Review | Expected behavior |
| --- | --- | --- |
| structure | Read headings, content and the app shell in order | Meaningful headings and navigation; decorative icons do not interrupt reading or duplicate a control name. Web header/main/footer landmarks are reachable. |
| actions | Activate buttons, links, icon buttons, chips and Save preferences | Names describe their action; disabled controls cannot activate; state changes are announced or discoverable. Links retain their URL/open-in-new-tab behavior on HTML. |
| forms | Enter/edit task names and notes; toggle checkbox, switch and radio controls | Labels, roles, values and selected/checked/disabled states are distinguishable. Keyboard/input focus remains on the active control. |
| selection | Change Delivery and Delivery address; adjust both price-range thumbs | Disabled delivery options cannot be chosen. Each thumb has a distinct Minimum/Maximum price name; values stay ordered and remain adjustable with assistive technology. |
| feedback | Save preferences, read the snackbar and activate Reset | Feedback is discoverable without stealing focus; Reset changes the controls; dismissal does not leave focus stranded. Progress state is distinguishable from completed content. |
| overlays | Open/dismiss menus, dialogs, bottom sheets and tooltips | Modal content is reachable and named; background interaction is blocked where appropriate; dismissal returns focus to a meaningful control. |
| collections | Scroll task/product lists and grids; reopen an item | Items remain discoverable in a meaningful order. Stable item identity preserves state; off-screen native lazy content becomes reachable when scrolling. |
| navigation | Switch destinations, open a task, edit a draft, go back and rotate | Selected destinations are announced. Drafts survive the supported transitions; focus does not jump to unrelated or removed content. Browser back/forward follows the renderer's documented support. |
| large-text | Increase font/display size, rotate and use phone/tablet layouts | Text and controls remain readable and reachable; overlapping, clipped or unreachable content is a failure. Intentional scrolling is usable. |
| touch-keyboard | Traverse and activate controls using touch, keyboard and reader gestures | Focus is visible where relevant, follows a useful order and is not trapped. Essential actions do not require hover. |
| theme | Review the default palette, custom provider and product palette in OS light/dark modes | Labels, selected states and focus remain distinguishable. Record the actual app palette: OS dark mode does not automatically replace application-owned tokens. Review a supplied dark palette when adopting one. |
| shop-flow | Browse, open a product, add it to the cart and complete the local checkout | Product images have useful descriptions; cart quantity, prices and errors are readable; the whole flow is operable with the reader. |

Theme review should consider [text contrast](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html) and [non-text contrast](https://www.w3.org/WAI/WCAG22/understanding/non-text-contrast.html). A palette check alone does not establish whole-app conformance.

## Component coverage

Use this inventory when reviewing a custom provider or adding a component. Structural components are reviewed through the screens that contain them; interactive components also need the relevant actions above.

| Family | Contracts |
| --- | --- |
| Structure and appearance | Theme, Column, Row, FlowRow, Box, Scaffold, Surface, Divider, Spacer |
| Collections | LazyColumn, LazyRow, LazyGrid |
| Content | Text, Image, Icon, Avatar, Badge |
| Actions | Button, Link, IconButton, FloatingActionButton, Chip, Card, Tooltip |
| Forms and selection | TextField, Checkbox, Switch, RadioButton, Slider, RangeSlider, Select, SegmentedButtons |
| Feedback | Banner, Progress, Snackbar |
| Navigation and overlays | TopAppBar, Tabs, NavigationBar, Menu, Dialog, BottomSheet |

## Record the results

Use the repository's **Accessibility and device review** issue form, or copy this report into a review:

```text
App version/build and commit:
Renderer (Android / iOS / HTML / Wasm / desktop):
Physical device or simulator:
Device model and OS:
Browser and screen reader/version:
Font/display size and app palette:
Review date:

structure: Not run
actions: Not run
forms: Not run
selection: Not run
feedback: Not run
overlays: Not run
collections: Not run
navigation: Not run
large-text: Not run
touch-keyboard: Not run
theme: Not run
shop-flow: Not run

Failures: control, steps, expected result, actual result.
Not-run checks: reason and needed environment.
```

A report applies to its recorded commit, device and configuration. Recheck affected controls after fixes. Keep **Not run** visible and report failures separately from automated CI results. Do not include device serial numbers, UDIDs or signing credentials.
