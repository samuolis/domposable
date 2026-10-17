# Compare semantic HTML and Wasm

Both previews compile the same shared feature sources, theme tokens, application-owned models and navigation recipes. Choose a viewport and feature to compare how each renderer responds. Open each preview separately for browser controls and full-window testing.

Wasm is an experimental visual comparison. Its stable CMP accessibility bridge has [known role, state and modal limitations](wasm.md); choose semantic HTML for accessible web forms.

[HTML demo](https://samuolis.github.io/domposable/demo/) · [Wasm demo](https://samuolis.github.io/domposable/wasm/) · [Path URLs](https://samuolis.github.io/domposable/routes/tasks/welcome/)

<label for="preview-width">Preview width</label>
<select id="preview-width"><option value="390">Phone · 390 px</option><option value="768">Tablet · 768 px</option><option value="640">Component panel · 640 px</option><option value="1440" selected>Wide · 1440 px</option></select>
<label for="preview-route">Feature</label>
<select id="preview-route"><option value="tasks">Tasks</option><option value="catalog">Components</option><option value="components">Selection and feedback</option><option value="recipes">Navigation recipes</option><option value="shop">Fieldwork storefront</option><option value="shop/collection">Shop collection</option><option value="compare/states">Material · states</option><option value="compare/actions">Material · actions</option><option value="compare/selection">Material · selection</option><option value="compare/inputs">Material · inputs</option><option value="compare/surfaces">Material · surfaces</option><option value="compare/navigation">Material · navigation</option><option value="compare/feedback">Material · feedback</option><option value="compare/gestures">Material · gestures</option><option value="compare/scaffold">Material · scaffold</option></select>
<label for="preview-theme">Component theme</label>
<select id="preview-theme"><option value="light">Light</option><option value="dark">Dark</option><option value="brand-light">Brand Light</option><option value="brand-dark">Brand Dark</option><option value="overrides-light">Overrides Light</option><option value="overrides-dark">Overrides Dark</option><option value="expressive-light">Expressive Light</option><option value="expressive-dark">Expressive Dark</option></select>
<p>The theme selector applies to the Material comparison scenes. Both frames receive the same explicit theme and component calls; the states scene shows enabled, disabled, checked, unchecked, mixed and slider endpoints together.</p>
<div class="comparison">
  <section><h2>Semantic HTML · Kotlin/JS</h2><div class="preview-scroll"><iframe title="Domposable semantic HTML preview" src="../demo/#/tasks" loading="lazy"></iframe></div></section>
  <section><h2>Compose UI · Kotlin/Wasm</h2><div class="preview-scroll"><iframe title="Domposable Wasm preview" src="../wasm/#/tasks" loading="lazy"></iframe></div></section>
</div>
<style>.comparison{display:grid;grid-template-columns:repeat(auto-fit,minmax(min(100%,400px),1fr));gap:16px}.preview-scroll{overflow:auto;border:1px solid #80759e;border-radius:12px}.comparison iframe{width:1440px;height:900px;border:0;display:block}</style>
<script>
(() => {
  const width = document.getElementById('preview-width');
  const route = document.getElementById('preview-route');
  const theme = document.getElementById('preview-theme');
  const frames = [...document.querySelectorAll('.comparison iframe')];
  width.addEventListener('change', () => frames.forEach(frame => {
    frame.style.width = width.value + 'px';
    frame.style.height = width.value === '390' ? '844px' : '900px';
  }));
  const update = () => {
    const path = route.value.startsWith('compare/')
      ? 'material/compare/' + theme.value + '/' + route.value.slice('compare/'.length)
      : route.value;
    frames.forEach((frame, index) => {
      // Reload the host so both renderers initialize the same theme and state.
      frame.src = (index ? '../wasm/' : '../demo/') + '?preview=' + encodeURIComponent(path) + '#/' + path;
    });
  };
  route.addEventListener('change', update);
  theme.addEventListener('change', update);
})();
</script>

CI captures both the top and bottom of each component family so lower controls, including time pickers and wheels, remain covered. The states fixture also compares checkbox, switch, radio and slider hover, press and keyboard focus across the eight themes. The stable Wasm accessibility bridge exposes slider labels without their bounds, so those pointer captures use the shared fixture's measured HTML coordinates and native keyboard traversal. This is canvas interaction coverage, not semantic-slider accessibility support.

The side-by-side frames intentionally scroll horizontally when their selected viewport exceeds the space on this page. This preserves the chosen **actual feature width**, rather than scaling pixels and pretending it is a phone layout.

HTML renders semantic elements and native browser controls. Wasm renders Compose UI and offers different text, input and accessibility behavior. The isolated Material scenes remove application chrome so control geometry, palettes and states can be compared directly at equal viewports. Renderer comparison reports expose remaining differences; an independent golden image passing does not prove HTML/Compose parity. Text rasterization and platform input behavior still depend on the renderer. See the [compatibility table](compatibility.md) for tested behavior, browser-history differences and current limitations.


## Reproducible component comparisons

The Material routes pin the same regular, medium and bold Roboto font files in both hosts, so font metrics do not depend on the browser or operating system. The library does not bundle a font or change your native Material font family: configure native typography in the host and use the matching CSS family through `DomposableMaterialTheme(typography = ...)` for HTML. The comparison host demonstrates both sides.

CI captures all nine component families in eight themes at 390 px and 1280 px. It captures the beginning and end of long families, plus hover, press and keyboard focus for checkbox, switch, radio and slider. The resulting 464 HTML / pixel difference / Wasm panels are exported in the CI evidence artifact. These diagnostic panels complement regression baselines; producing an image does not establish visual parity.

The demo font files come from [Roboto](https://github.com/googlefonts/roboto-2/tree/38062f4b4a0be4346d07a928408da21602545e9e), with the Apache 2.0 license included in the demo resources. They apply only to comparison routes.
