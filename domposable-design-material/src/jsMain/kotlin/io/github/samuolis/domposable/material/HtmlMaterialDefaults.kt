package io.github.samuolis.domposable.material

// Material defaults stay below per-call inline appearance and modifier overrides.
internal const val MATERIAL_DEFAULTS_CSS = """
.d-theme .d-button,.d-theme .d-chip,.d-theme .d-segment-surface,.d-theme .d-multi-segment,.d-theme .d-tab{
 font-size:var(--d-type-label-large-size);line-height:var(--d-type-label-large-line-height);
 font-weight:var(--d-type-label-large-weight);letter-spacing:var(--d-type-label-large-letter-spacing)
}
.d-button{min-width:58px;min-height:40px;padding:8px 24px;border:0}
.d-button-outlined{border:1px solid var(--d-outline-variant,var(--d-outline));padding:7px 23px;color:var(--d-on-surface-variant)}
.d-button-text{padding-inline:12px}
.d-button-toggle:not([aria-pressed=true]){background:var(--d-surface-container);color:var(--d-on-surface-variant)}
.d-button-toggle.d-button-outlined:not([aria-pressed=true]){background:transparent}
.d-button-toggle.d-button-elevated:not([aria-pressed=true]){background:var(--d-surface-container-low)}
.d-button-toggle.d-button-filledtonal:not([aria-pressed=true]){background:var(--d-secondary-container)}
.d-button-toggle.d-button-filled[aria-pressed=true]{background:var(--d-primary);color:var(--d-on-primary)}
.d-button-toggle.d-button-outlined[aria-pressed=true]{background:var(--d-inverse-surface);color:var(--d-inverse-on-surface)}
.d-chip{padding:var(--d-item-padding,4px 12px);background:var(--d-item-container,transparent);border-color:var(--d-item-border,var(--d-outline-variant));color:var(--d-item-content,var(--d-on-surface-variant))}
.d-chip[aria-pressed=true]{background:var(--d-item-selected-container,var(--d-secondary-container));color:var(--d-item-selected-content,var(--d-on-secondary-container));border-color:var(--d-item-selected-border,transparent)}
.d-chip-elevated{background:var(--d-item-container,var(--d-surface-container-low));border-color:var(--d-item-border,transparent)}
.d-chip.d-item-hittarget .d-chip-surface{padding:var(--d-item-padding,4px 12px);background:var(--d-item-container,transparent);border-color:var(--d-item-border,var(--d-outline-variant));color:var(--d-item-content,var(--d-on-surface-variant))}
.d-chip-elevated.d-item-hittarget .d-chip-surface{background:var(--d-item-container,var(--d-surface-container-low));border-color:var(--d-item-border,transparent)}
.d-card{padding:0}
.d-chip-label{gap:8px}.d-chip-icon{margin-inline-end:8px}
.d-theme .d-segment-surface{border-radius:0;min-height:var(--d-item-min-height,40px);padding:var(--d-item-padding,9px 12px)}
.d-theme .d-segment:first-child .d-segment-surface{border-start-start-radius:var(--d-item-radius-start-start,var(--d-item-radius,999px));border-end-start-radius:var(--d-item-radius-end-start,var(--d-item-radius,999px))}
.d-theme .d-segment:last-child .d-segment-surface{border-start-end-radius:var(--d-item-radius-start-end,var(--d-item-radius,999px));border-end-end-radius:var(--d-item-radius-end-end,var(--d-item-radius,999px))}
.d-segment+.d-segment{margin-inline-start:-1px}
.d-segment input:checked+.d-segment-surface{color:var(--d-item-selected-content,var(--d-on-secondary-container));border-color:var(--d-item-selected-border,var(--d-outline))}
.d-multi-segment{min-height:var(--d-item-min-height,40px);padding:var(--d-item-padding,9px 12px);color:var(--d-item-content,var(--d-on-surface))}
.d-multi-segment+.d-multi-segment{margin-inline-start:-1px}
.d-multi-segment:first-child{border-start-start-radius:var(--d-item-radius,999px);border-end-start-radius:var(--d-item-radius,999px)}
.d-multi-segment:last-child{border-start-end-radius:var(--d-item-radius,999px);border-end-end-radius:var(--d-item-radius,999px)}
.d-multi-segment[aria-pressed=true]{background:var(--d-item-selected-container,var(--d-secondary-container));color:var(--d-item-selected-content,var(--d-on-secondary-container));border-color:var(--d-item-selected-border,var(--d-outline))}
.d-fab{padding:0;background:var(--d-primary-container);color:var(--d-on-primary-container)}
.d-fab-small{border-radius:var(--d-shape-medium,12px)}.d-fab-medium{border-radius:var(--d-shape-large,20px)}.d-fab-large{border-radius:var(--d-shape-extra-large,28px)}
.d-app-bar .d-app-bar-title{font-size:var(--d-type-title-large-size);line-height:var(--d-type-title-large-line-height);font-weight:var(--d-type-title-large-weight);letter-spacing:var(--d-type-title-large-letter-spacing)}
.d-app-bar-medium .d-app-bar-title,.d-app-bar-mediumflexible .d-app-bar-title{font-size:var(--d-type-headline-small-size);line-height:var(--d-type-headline-small-line-height)}
.d-app-bar-large .d-app-bar-title,.d-app-bar-largeflexible .d-app-bar-title{font-size:var(--d-type-headline-medium-size);line-height:var(--d-type-headline-medium-line-height)}
.d-tabs{width:100%;gap:0;border-bottom:1px solid var(--d-outline-variant)}
.d-tabs .d-tab{flex:1;min-width:0;min-height:48px;justify-content:center;position:relative;padding:0 16px;border:0}
.d-tabs .d-tab[aria-selected=true]{color:var(--d-item-selected-content,var(--d-primary));border:0}
.d-tabs .d-tab[aria-selected=true]::after{content:"";position:absolute;inset-inline:0;bottom:0;height:var(--d-tabs-indicator-height,3px);background:var(--d-item-selected-border,var(--d-primary))}
.d-tabs-primary .d-tab[aria-selected=true]::after{width:40px;inset-inline:calc(50% - 20px);border-radius:3px 3px 0 0}
.d-tabs-secondary .d-tab[aria-selected=true]::after{height:2px}
.d-theme .d-navigation-rail{width:96px;gap:0;padding-block:12px;background:var(--d-surface);align-items:stretch}
.d-theme .d-navigation-rail>button,.d-theme .d-navigation-rail>a{display:flex;flex-direction:column;justify-content:center;gap:4px;min-height:80px;padding:8px 0;font-size:12px;line-height:16px;border:0;border-radius:0;background:transparent}
.d-theme .d-navigation-rail>[aria-current=page]{border:0;background:transparent;color:var(--d-item-selected-content,var(--d-on-secondary-container))}
.d-navigation-rail>button>span[aria-hidden=true],.d-navigation-rail>a>span[aria-hidden=true]{display:grid;place-items:center;width:56px;height:32px;border-radius:999px}
.d-navigation-rail>[aria-current=page]>span[aria-hidden=true]{background:var(--d-item-selected-container,var(--d-secondary-container))}
.d-wide-navigation-rail:not(.d-wide-navigation-rail-expanded){width:96px;flex-basis:96px;align-items:stretch;gap:0;padding-block:12px}
.d-wide-navigation-rail-item-compact{flex-direction:column;gap:4px;min-height:80px;padding:8px 0;font-size:12px;line-height:16px;border-radius:0;background:transparent}
.d-wide-navigation-rail-item-compact[aria-current=page]{background:transparent}
.d-rail-icon{display:grid;place-items:center;width:56px;height:32px;border-radius:999px}
.d-wide-navigation-rail-item-compact[aria-current=page] .d-rail-icon{background:var(--d-item-selected-container,var(--d-secondary-container))}
.d-theme .d-toggle input:focus-visible,.d-theme .d-slider:focus-visible{outline:none}
.d-toggle:has(input:enabled:focus-visible),.d-slider-surface:has(input:focus-visible),.d-range:has(input:focus-visible){outline:3px solid var(--d-primary);outline-offset:3px}
.d-slider:focus-visible::-webkit-slider-thumb{width:2px}.d-slider:focus-visible::-moz-range-thumb{width:2px}
.d-slider-vertical:focus-visible::-webkit-slider-thumb{width:44px;height:2px}.d-slider-vertical:focus-visible::-moz-range-thumb{width:44px;height:2px}
@media(forced-colors:active){.d-toggle:has(input:enabled:focus-visible),.d-slider-surface:has(input:focus-visible),.d-range:has(input:focus-visible){outline:2px solid Highlight}}
"""
