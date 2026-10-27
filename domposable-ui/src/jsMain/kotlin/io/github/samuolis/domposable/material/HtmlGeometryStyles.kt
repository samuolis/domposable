package io.github.samuolis.domposable.material

// Aggregated by the material HTML style installer with the other reusable component styles.
internal const val DOMPOSABLE_HTML_GEOMETRY_CSS = """
.d-timeline-canvas{display:flex;flex-direction:column;min-width:0;min-height:0;width:100%;height:100%;color:var(--d-on-surface)}
.d-timeline-canvas-header{position:sticky;top:0;z-index:5;display:grid;min-height:48px;width:max-content;min-width:100%;background:var(--d-surface-variant)}
.d-timeline-canvas-scroll{flex:1 1 0;min-height:0;min-width:0;overflow:auto;overscroll-behavior:contain;touch-action:pan-x pan-y;-webkit-overflow-scrolling:touch}
.d-timeline-canvas-body{position:relative;display:grid;width:max-content;min-width:max-content}
.d-timeline-canvas-ruler{position:relative;background:var(--d-background);opacity:1}
.d-timeline-canvas-marker{position:absolute;left:0;z-index:2;font-size:.75rem;transform:translateY(-50%);background:var(--d-background)}
.d-timeline-canvas-lane{position:relative;min-width:120px;border-inline-start:0;background:var(--d-background)}
.d-timeline-canvas-line{position:absolute;left:0;right:0;z-index:1;border-top:1px solid var(--d-outline-variant,var(--d-outline));pointer-events:none}
.d-timeline-canvas-item{position:absolute;left:4px;right:4px;z-index:2;overflow:hidden;min-height:72px;cursor:pointer}
.d-timeline-canvas-item[draggable=true]{cursor:grab}.d-timeline-canvas-item:focus-visible{outline:2px solid var(--d-primary);outline-offset:2px}
.d-timeline-canvas-now{position:absolute;left:0;right:0;z-index:3;border-top:2px solid var(--d-tertiary);pointer-events:none}
.d-timeline-drop-preview{position:absolute;z-index:5;box-sizing:border-box;border:2px dashed var(--d-primary);border-radius:var(--d-shape-small,8px);background:transparent;pointer-events:none}
.d-timeline-canvas-calendar .d-timeline-drop-preview{border-radius:var(--d-shape-medium,12px)}
.d-timeline-canvas-calendar .d-timeline-canvas-ruler{background:color-mix(in srgb,var(--d-surface-variant) 30%,transparent)}
.d-timeline-canvas-calendar .d-timeline-canvas-calendar-lane{box-sizing:border-box;border-inline-start:1px solid var(--d-outline-variant,var(--d-outline))}
.d-timeline-canvas-calendar .d-timeline-canvas-lane-header{overflow:hidden;white-space:nowrap;text-overflow:ellipsis}
.d-timeline-canvas-calendar .d-timeline-canvas-calendar-lane .d-timeline-canvas-item{left:8px;right:8px;box-sizing:border-box;min-height:120px;padding:2px 0;border:0;border-radius:0;background:transparent;overflow:visible;cursor:default}
.d-timeline-canvas-calendar .d-timeline-canvas-calendar-lane .d-timeline-canvas-item:focus-visible{outline:0}
.d-timeline-canvas-item.is-dragging{z-index:4;opacity:.88;transform:scale(.99);box-shadow:0 6px 18px color-mix(in srgb,var(--d-on-surface) 18%,transparent)}
.d-timeline-canvas-lane::before{content:"";position:absolute;inset-block:0;inset-inline-start:0;width:1px;background:var(--d-outline-variant,var(--d-outline));pointer-events:none}
.d-timeline-canvas-body::after{content:"";position:absolute;inset-block:0;inset-inline-end:0;width:1px;background:var(--d-outline-variant,var(--d-outline));pointer-events:none}
.d-timeline-canvas-follow-space{flex:none}
.d-timeline-canvas-responsive-compact-lanes{--d-timeline-lane-track:minmax(200px,1fr)}
@media(max-width:599px){.d-timeline-canvas-responsive-compact-lanes{--d-timeline-lane-track:200px}}
.d-timeline-item-card-muted-metadata .d-text{color:color-mix(in srgb,var(--d-on-surface) 70%,transparent)}
.d-timeline-grid{display:grid;gap:8px;overflow-x:auto}
.d-timeline-row{display:grid;grid-template-columns:max-content repeat(var(--d-timeline-columns),minmax(5rem,1fr));gap:8px;border-block-end:1px solid var(--d-outline-variant,var(--d-outline))}
.d-timeline-row>:not(:first-child){border-inline-start:1px solid var(--d-outline-variant,var(--d-outline))}
.d-timeline-cell{min-width:0;box-sizing:border-box;padding-inline-start:4px}
.d-matrix-scroll{border-radius:var(--d-radius);overscroll-behavior-inline:contain;--d-matrix-cell-height:56px}
.d-matrix-compact{--d-matrix-cell-height:48px}
.d-matrix-expanded{--d-matrix-cell-height:56px}
.d-matrix-table{border-collapse:collapse;table-layout:fixed}
.d-matrix-cell{box-sizing:border-box;vertical-align:middle}
.d-matrix-row-label>.d-text{overflow-wrap:anywhere}
.d-matrix-overlay-icon{position:absolute;inset-block-start:0;inset-inline-end:0;line-height:0}
.d-zoomable-diagram-viewport{max-width:100%;min-height:16rem;overflow:auto;border:1px solid var(--d-outline-variant,var(--d-outline));border-radius:var(--d-shape-medium,12px);background:var(--d-surface)}
.d-zoomable-diagram-canvas{position:relative;display:grid;grid-template-columns:repeat(var(--d-diagram-columns),minmax(7rem,1fr));grid-template-rows:repeat(var(--d-diagram-rows),minmax(5.5rem,auto));min-width:max(100%,calc(var(--d-diagram-columns)*7rem));transform-origin:top left}
.d-zoomable-diagram-standard .d-zoomable-diagram-canvas{grid-template-rows:repeat(var(--d-diagram-rows),minmax(7.5rem,auto))}
.d-zoomable-diagram-expanded .d-zoomable-diagram-canvas{grid-template-rows:repeat(var(--d-diagram-rows),minmax(10rem,auto))}
.d-zoomable-diagram-compact .d-zoomable-diagram-canvas{grid-template-rows:repeat(var(--d-diagram-rows),minmax(5.5rem,auto))}
.d-zoomable-diagram-node{z-index:1;min-width:0;cursor:pointer}
.d-zoomable-diagram-node:focus-visible,.d-zoomable-diagram-viewport:focus-visible{outline:2px solid var(--d-primary);outline-offset:2px}
.d-zoomable-diagram-connector{position:absolute;z-index:0;height:2px;transform-origin:left center;background:var(--d-outline-variant,var(--d-outline));pointer-events:none}
.d-image-crop-dialog{min-width:min(420px,calc(100vw - 64px))}
.d-image-crop-viewport{position:relative;flex:none;max-width:100%;margin:0 auto;overflow:hidden;border-radius:var(--d-shape-medium,12px);background:#111;touch-action:pan-y}
.d-image-crop-source{width:100%;height:100%;object-fit:cover;transition:transform .12s ease-out}
.d-image-crop-frame{position:absolute;inset:0;border:1px solid rgb(255 255 255 / .9);box-shadow:0 0 0 999px rgb(0 0 0 / .18);pointer-events:none}
.d-image-crop-frame::after{position:absolute;inset:0;content:"";background:linear-gradient(to right,transparent 33.1%,rgb(255 255 255 / .55) 33.3%,transparent 33.6%,transparent 66.4%,rgb(255 255 255 / .55) 66.7%,transparent 66.9%),linear-gradient(to bottom,transparent 33.1%,rgb(255 255 255 / .55) 33.3%,transparent 33.6%,transparent 66.4%,rgb(255 255 255 / .55) 66.7%,transparent 66.9%)}
@media(max-width:600px){.d-image-crop-dialog{min-width:0;width:min(100%,calc(100vw - 80px))}.d-image-crop-actions{flex-direction:column-reverse!important}.d-image-crop-actions button{width:100%}}
"""
