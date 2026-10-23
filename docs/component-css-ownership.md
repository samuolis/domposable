# HTML component CSS ownership

Reusable HTML component geometry belongs in `domposable-design-material` alongside the HTML
renderer. The shared material HTML style installer includes the component stylesheet, so consumers
receive the styles through the normal theme provider.

Library selectors use the `d-*` namespace and scope layout to existing semantic component roots.
The classes describe reusable structure or visual variants, such as `.d-timeline-canvas`,
`.d-timeline-canvas-calendar`, `.d-matrix-scroll`, and `.d-zoomable-diagram-viewport`. A consumer
may choose a neutral variant from its own state, but the library does not name the product or reason
for that choice.

The renderer owns layout, scrolling, focus geometry, and responsive sizing. Existing component
nodes keep their semantics, event handlers, accessibility attributes, and item content. Use the
component's existing root and child nodes for CSS hooks; do not add a wrapper solely to style them.
Dynamic values use the library's `--d-*` tokens and narrowly scoped component properties such as
`--d-matrix-cell-height`, `--d-timeline-columns`, and `--d-diagram-rows`.

Keep app branding and business behavior in the consumer. Library CSS must not depend on Ranker
selectors or colors, sports, events, players, organizations, or other product concepts. Consumers
can still provide a brand token at an app-shell boundary, while the library uses generic `--d-*`
tokens inside reusable components.

The material provider installs these hooks; applications must not ship a second stylesheet that
repairs the same components:

| Component | Root hooks |
| --- | --- |
| Custom editor and rich documents | `d-rich-text-editor`, `d-rich-html` |
| Selection, cells and action slots | `d-selection-row`, `d-cell-plain`, `d-action-dialog-actions` |
| Loading and custom labels | `d-skeleton`, `d-button-label`, `d-visually-hidden` |
| Timeline, matrix and diagram | `d-timeline-canvas`, `d-matrix-scroll`, `d-zoomable-diagram-viewport` |
| Image crop and upload | `d-image-crop-viewport`, `d-image-upload-preview` |
| Calendar and time controls | `d-picker-calendar-controls`, `d-picker-time` |
| Media and motion | `d-media-marquee`, `d-collapsible-layout`, `d-confetti`, `d-status-illustration` |
| Sticky actions and navigation | `d-bottom-bar`, `d-bottom-bar-desktop-inline`, `d-contextual-navigation`, `d-floating-action-toolbar` |
| Semantic shell slots | `d-app-header`, `d-web-footer`, `d-navigation-panel`, `d-breadcrumbs` |

Theme colors, typography, card/menu shadows and breadcrumb widths remain configurable through
tokens and component custom properties. Dynamic data coordinates and application navigation policy remain with their caller.

Inputs can opt into `d-body-input` for default 16px body text, with fixed 16px control text on phones
to avoid focus zoom. Ordinary fields keep their configured typography. This opt-in belongs to the
consumer's presentation configuration.

A layout's `gap` is its minimum spacing, including `SpaceBetween`, `SpaceAround` and `SpaceEvenly`.
The HTML renderer uses flex gaps; native arrangements preserve the same minimum in both layout
directions. A flow row wraps when its children and the requested gap cannot fit on one line.

## One contract for native and HTML

Applications use the same typed component variants, appearance values and modifiers on each target.
A missing generic option belongs in that contract and both renderers. Do not implement it as an
application-only CSS repair. For example, card shadows belong to `DomposableSurfaceAppearance`,
and icon/label spacing belongs to `DomposableButtonAppearance.contentSpacing`.

Custom tab labels, top-app-bar titles, app-bar actions and snackbar content are component slots;
the library still owns their container, interaction and accessibility behavior. Passive selection
controls accept a null callback and retain their state without inventing a click action.

Native interoperability overloads in `domposable-design-material` support annotated text/layout
measurements, resource-backed typography, painter artwork and keyboard/transformation options.
Keep these calls in the consumer's native source set. Shared feature declarations continue using
the portable component APIs; native types must not leak into a second HTML implementation.
