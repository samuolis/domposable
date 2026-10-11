# Alpha coverage

| Family | Shared API and native/HTML implementations |
| --- | --- |
| Layout | Theme, Column, Row, Box, Scaffold, measure, ordered modifiers |
| Collections | Keyed LazyColumn, LazyRow, LazyGrid |
| Content | Text/headings, Image, Icon, Divider, Spacer, Avatar, Badge |
| Actions | Button, IconButton, Chip, Card |
| Forms | TextField, Checkbox, Switch, RadioButton, Slider |
| Feedback | Banner, Progress, Surface |
| Navigation | Tabs, NavigationBar, Menu |
| Overlays | Dialog, BottomSheet |
| Architecture | Route registry, entry resources/saveables, optional Navigation 3, lifecycle and KSP |

The initial API has 32 renderer contracts. HTML uses native input elements, headings, landmarks, labeled dialog elements, tabs with keyboard navigation and focus restoration. Native providers use Compose UI/Material3 semantics. Browser accessibility checks run in Chromium, Firefox and WebKit. Native demo smoke tests run on an iOS simulator; shared rules are tested on JVM.

HTML keyed collections currently render all items. Virtualized lists, rich text editors, drag/reorder, complex date pickers, fold/hinge APIs, platform camera/maps and product-specific widgets are outside the initial provider. Date/time HTML inputs use browser controls; native text fields currently expose text input for those purposes. Scrolling and sizing use each platform's layout model and are not pixel-identical.

The default design provider uses upstream Material3 alpha APIs matching the current Compose release. JVM desktop is experimental. There is no compatibility promise before a stable Domposable release. KSP currently generates registration factories, not screen business logic or DI graphs.
