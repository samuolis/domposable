# Compare semantic HTML and Wasm

Both previews compile the same shared feature sources, theme tokens, application-owned models and navigation recipes. Choose a viewport and feature to compare how each renderer responds. Open each preview separately for browser controls and full-window testing.

Wasm is an experimental visual comparison. Its stable CMP accessibility bridge has [known role, state and modal limitations](wasm.md); choose semantic HTML for accessible web forms.

[HTML demo](https://samuolis.github.io/domposable/demo/) · [Wasm demo](https://samuolis.github.io/domposable/wasm/) · [Path URLs](https://samuolis.github.io/domposable/routes/tasks/welcome/)

<label for="preview-width">Preview width</label>
<select id="preview-width"><option value="390">Phone · 390 px</option><option value="768">Tablet · 768 px</option><option value="1440" selected>Wide · 1440 px</option></select>
<label for="preview-route">Feature</label>
<select id="preview-route"><option value="tasks">Tasks</option><option value="catalog">Components</option><option value="components">Selection and feedback</option><option value="recipes">Navigation recipes</option><option value="shop">Fieldwork storefront</option><option value="shop/collection">Shop collection</option></select>
<div class="comparison">
  <section><h2>Semantic HTML · Kotlin/JS</h2><div class="preview-scroll"><iframe title="Domposable semantic HTML preview" src="../demo/#/tasks" loading="lazy"></iframe></div></section>
  <section><h2>Compose UI · Kotlin/Wasm</h2><div class="preview-scroll"><iframe title="Domposable Wasm preview" src="../wasm/#/tasks" loading="lazy"></iframe></div></section>
</div>
<style>.comparison{display:grid;grid-template-columns:repeat(auto-fit,minmax(min(100%,400px),1fr));gap:16px}.preview-scroll{overflow:auto;border:1px solid #80759e;border-radius:12px}.comparison iframe{width:1440px;height:900px;border:0;display:block}</style>
<script>
(() => {
  const width = document.getElementById('preview-width');
  const route = document.getElementById('preview-route');
  const frames = [...document.querySelectorAll('.comparison iframe')];
  width.addEventListener('change', () => frames.forEach(frame => {
    frame.style.width = width.value + 'px';
    frame.style.height = width.value === '390' ? '844px' : '900px';
  }));
  route.addEventListener('change', () => frames.forEach((frame, index) => {
    // A hash-only change would keep the previous application shell mounted.
    // Changing the fixture query forces both hosts to read the initial route.
    frame.src = (index ? '../wasm/' : '../demo/') + '?preview=' + encodeURIComponent(route.value) + '#/' + route.value;
  }));
})();
</script>

The side-by-side frames intentionally scroll horizontally when their selected viewport exceeds the space on this page. This preserves the chosen **actual feature width**, rather than scaling pixels and pretending it is a phone layout.

HTML renders semantic elements and native browser controls. Wasm renders Compose UI and offers different text, input and accessibility behavior. Layouts are intentionally adaptive, not pixel-identical. See the [compatibility table](compatibility.md) for tested behavior, browser-history differences and current limitations.
