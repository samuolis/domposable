package io.github.samuolis.domposable.material

internal const val HTML_CONTROL_SLOT_CSS = """
.d-selection-row{display:flex;align-items:center;gap:14px;min-height:48px;padding:8px 0;cursor:pointer;font-size:var(--d-selection-label-size,14px);line-height:var(--d-selection-label-line-height,17px)}
.d-selection-row input{flex:none;margin:0 14px;cursor:inherit}
.d-selection-disabled{cursor:default}
.d-cell-plain{display:block;box-sizing:border-box;min-width:0;width:100%;padding:8px 0;background:transparent;color:var(--d-on-surface);border:0;border-radius:0;box-shadow:none}
.d-cell-plain.d-selection-row{display:block;min-height:0}
.d-cell-plain.d-selection-row input{margin:0 14px}
.d-cell-disabled{opacity:.56}
.d-section-selection{display:flex;box-sizing:border-box;align-items:center;justify-content:space-between;gap:12px;width:100%;min-height:80px;margin-top:0;padding:24px 0 8px;color:var(--d-on-surface);background:transparent;border:0;border-radius:0;box-shadow:none}
.d-section-selection input{margin:0 14px}
:is(.d-section-selection,.d-cell-plain).d-selection-row:not(.d-cell-disabled){-webkit-tap-highlight-color:transparent;transition:background-color .12s ease}
@media(hover:hover){:is(.d-section-selection,.d-cell-plain).d-selection-row:not(.d-cell-disabled):hover{background:color-mix(in srgb,var(--d-on-surface) 8%,transparent)}}
:is(.d-section-selection,.d-cell-plain).d-selection-row:not(.d-cell-disabled):active{background:color-mix(in srgb,var(--d-on-surface) 12%,transparent)}
@media(prefers-reduced-motion:reduce){:is(.d-section-selection,.d-cell-plain).d-selection-row{transition:none}}
.d-select-option.d-cell-plain{display:flex}
.d-select-option:focus-within{outline:2px solid var(--d-primary);outline-offset:2px}
@media(forced-colors:active){.d-select-option:focus-within{outline:2px solid Highlight;outline-offset:2px}}
.d-action-dialog-actions .d-button{max-width:100%;height:auto}
.d-action-dialog-actions .d-button.d-button-single-line .d-button-copy{white-space:normal;text-align:center}
.d-action-dialog-three-actions .d-button{width:100%}
.d-field-error{color:var(--d-error);font-size:var(--d-type-body-small-size);line-height:var(--d-type-body-small-line-height)}
.d-avatar-content{max-width:100%;max-height:100%;vertical-align:middle;line-height:0}
.d-provider-button img{object-fit:contain;position:absolute;inset-inline-start:16px;top:50%;transform:translateY(-50%)}
.d-inline-badge-action{position:relative;cursor:pointer}
.d-inline-badge-action::after{content:"";position:absolute;inset-inline-start:50%;inset-block-start:50%;width:max(100%,var(--d-badge-touch-size));height:max(100%,var(--d-badge-touch-size));transform:translate(-50%,-50%)}
.d-inline-badge-action:focus-visible,[role=button]:focus-visible>.d-inline-badge-action{outline:2px solid var(--d-primary);outline-offset:2px}
@media(forced-colors:active){.d-inline-badge-action:focus-visible,[role=button]:focus-visible>.d-inline-badge-action{outline-color:Highlight}}
"""
