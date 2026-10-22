package io.github.samuolis.domposable.material

/** Styling hooks for custom content placed inside shared controls and loading placeholders. */
internal const val HTML_CONTENT_SLOT_CSS = """
.d-button-label{display:inline-flex;align-items:center;justify-content:center;gap:var(--d-button-label-gap,4px);white-space:nowrap}
.d-lazy-row-items>[data-row-key]{flex:0 0 auto;min-width:0}
.d-text-editorial[data-editorial-role=display]{font-size:clamp(36px,4.5vw,54px);line-height:1.12}
.d-text-editorial[data-editorial-role=headline]{font-size:36px;line-height:1.18}
.d-text-editorial[data-editorial-role=body]{font-size:21px;line-height:1.5}
@media(max-width:600px){.d-text-editorial[data-editorial-role=body]{font-size:18px}.d-text-editorial[data-editorial-role=headline]{font-size:28px}}
.d-skeleton{box-sizing:border-box;position:relative;display:block;flex:none;overflow:hidden;background:var(--d-shimmer-base,var(--d-variant))}
.d-skeleton[data-animate=true]::after{position:absolute;inset:0 auto 0 calc(-1 * var(--d-shimmer-band,60%));width:var(--d-shimmer-band,60%);content:"";background:linear-gradient(90deg,var(--d-shimmer-base,var(--d-variant)),var(--d-shimmer-base,var(--d-variant)) 25%,var(--d-shimmer-highlight,var(--d-surface)) 50%,var(--d-shimmer-base,var(--d-variant)) 75%,var(--d-shimmer-base,var(--d-variant)));animation:d-skeleton-shimmer var(--d-shimmer-duration,1200ms) linear infinite}
@keyframes d-skeleton-shimmer{to{left:100%}}
@media(prefers-reduced-motion:reduce){.d-skeleton::after{display:none}}
/* Compact inputs retain a mobile-safe font size to avoid focus zoom. */
[data-domposable-theme] input:not([type=checkbox]):not([type=radio]):not([type=range]):not([type=button]):not([type=submit]):not([type=reset]){font-size:16px}
@media(max-width:600px){.d-field-control>:is(input,textarea){font-size:16px!important}}

.d-bottom-sheet-scroll{overscroll-behavior:contain}
.d-bottom-sheet-list-item{flex:none;min-width:0;width:100%}
.d-bottom-sheet-content{--d-surface:var(--d-background)}
.d-bottom-sheet-content .d-dialog-heading .d-text{min-width:0;overflow-wrap:anywhere}
.d-bottom-sheet-content .d-dialog-heading .d-icon-button{flex:none}
.d-agreement-row{display:flex;align-items:center;gap:8px;min-width:0}
.d-agreement-row .d-link{text-underline-offset:2px}
.d-image-upload-preview{position:relative}
.d-image-upload-image{width:100%;height:100%;min-width:0}
.d-image-upload-file{position:absolute;inset:0;z-index:1;width:100%;height:100%;opacity:0;cursor:pointer}
.d-card-press-feedback:active{filter:brightness(.96)}
.d-segment-press-feedback .d-segment:active:not(:has(input:disabled)){transform:scale(.97)}
@media(prefers-reduced-motion:reduce){.d-segment-press-feedback .d-segment:active{transform:none}}
"""
