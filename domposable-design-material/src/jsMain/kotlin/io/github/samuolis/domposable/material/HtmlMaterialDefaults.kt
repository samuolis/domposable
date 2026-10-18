package io.github.samuolis.domposable.material

// Namespaced defaults also work in provider-only hosts; :root preserves theme-selector specificity.
internal const val MATERIAL_DEFAULTS_CSS = """
:root .d-button,:root .d-chip,:root .d-segment-surface,:root .d-multi-segment,:root .d-tab{
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
.d-chip:not(.d-item-hittarget){padding:var(--d-item-padding,3px 7px);background:var(--d-item-container,transparent);border-color:var(--d-item-border,var(--d-outline-variant));color:var(--d-item-content,var(--d-on-surface-variant))}
.d-chip:not(.d-item-hittarget)[aria-pressed=true],.d-chip.d-item-hittarget[aria-pressed=true] .d-chip-surface{background:var(--d-item-selected-container,var(--d-secondary-container));color:var(--d-item-selected-content,var(--d-on-secondary-container));border-color:var(--d-item-selected-border,transparent)}
.d-chip-elevated:not(.d-item-hittarget):not([aria-pressed=true]){background:var(--d-item-container,var(--d-surface-container-low));border-color:var(--d-item-border,transparent)}
.d-chip.d-item-hittarget .d-chip-surface{padding:var(--d-item-padding,3px 7px);background:var(--d-item-container,transparent);border-color:var(--d-item-border,var(--d-outline-variant));color:var(--d-item-content,var(--d-on-surface-variant))}
.d-chip-elevated.d-item-hittarget:not([aria-pressed=true]) .d-chip-surface{background:var(--d-item-container,var(--d-surface-container-low));border-color:var(--d-item-border,transparent)}
.d-card{padding:0}
.d-chip-label{gap:8px;min-width:0}.d-chip-icon{flex:none}
:root .d-segment-surface{border-radius:0;min-height:var(--d-item-min-height,40px);padding:var(--d-item-padding,9px 11px)}
:root .d-segment:first-child .d-segment-surface{border-start-start-radius:var(--d-item-radius-start-start,var(--d-item-radius,999px));border-end-start-radius:var(--d-item-radius-end-start,var(--d-item-radius,999px))}
:root .d-segment:last-child .d-segment-surface{border-start-end-radius:var(--d-item-radius-start-end,var(--d-item-radius,999px));border-end-end-radius:var(--d-item-radius-end-end,var(--d-item-radius,999px))}
.d-segment+.d-segment{margin-inline-start:-1px}
.d-segment input:checked+.d-segment-surface{color:var(--d-item-selected-content,var(--d-on-secondary-container));border-color:var(--d-item-selected-border,var(--d-outline))}
.d-multi-segment{min-height:var(--d-item-min-height,40px);padding:var(--d-item-padding,9px 11px);color:var(--d-item-content,var(--d-on-surface))}
.d-multi-segment+.d-multi-segment{margin-inline-start:-1px}
.d-multi-segment:first-child{border-start-start-radius:var(--d-item-radius,999px);border-end-start-radius:var(--d-item-radius,999px)}
.d-multi-segment:last-child{border-start-end-radius:var(--d-item-radius,999px);border-end-end-radius:var(--d-item-radius,999px)}
.d-multi-segment[aria-pressed=true]{background:var(--d-item-selected-container,var(--d-secondary-container));color:var(--d-item-selected-content,var(--d-on-secondary-container));border-color:var(--d-item-selected-border,var(--d-outline))}
.d-fab{padding:0;background:var(--d-primary-container);color:var(--d-on-primary-container)}
.d-fab-small{border-radius:var(--d-shape-medium,12px)}.d-fab-medium{border-radius:var(--d-shape-large,20px)}.d-fab-large{border-radius:var(--d-shape-extra-large,28px)}
.d-app-bar .d-app-bar-title{font-size:var(--d-type-title-large-size);line-height:var(--d-type-title-large-line-height);font-weight:var(--d-type-title-large-weight);letter-spacing:var(--d-type-title-large-letter-spacing)}
.d-app-bar-medium .d-app-bar-title{font-size:var(--d-type-headline-small-size);line-height:var(--d-type-headline-small-line-height)}
.d-app-bar-large .d-app-bar-title,.d-app-bar-mediumflexible .d-app-bar-title,.d-app-bar-tworows .d-app-bar-title{font-size:var(--d-type-headline-medium-size);line-height:var(--d-type-headline-medium-line-height)}
.d-tabs{width:100%;gap:0;border-bottom:0;box-shadow:inset 0 -1px var(--d-outline-variant)}
.d-tabs-primary .d-tab{color:var(--d-item-content,var(--d-primary))}.d-tabs-secondary .d-tab{color:var(--d-item-content,var(--d-on-surface))}
.d-tabs-scrollable{padding-inline:52px;overflow-x:auto}.d-tabs.d-tabs-scrollable .d-tab{flex:none;min-width:90px}
.d-tabs .d-tab{flex:1;min-width:0;min-height:var(--d-item-min-height,48px);justify-content:center;position:relative;padding:0 16px;border:0}
.d-tabs .d-tab[aria-selected=true]{color:var(--d-item-selected-content,var(--d-primary));border:0}.d-tabs-secondary .d-tab[aria-selected=true]{color:var(--d-item-selected-content,var(--d-on-surface))}
.d-tabs .d-tab[aria-selected=true]::after{content:"";position:absolute;inset-inline:0;bottom:0;height:var(--d-tabs-indicator-height,3px);background:var(--d-item-selected-border,var(--d-primary))}
.d-tabs-primary .d-tab[aria-selected=true]::after{width:40px;inset-inline:calc(50% - 20px);border-radius:3px 3px 0 0}
.d-tabs-secondary .d-tab[aria-selected=true]::after{height:2px}
:root .d-navigation.d-navigation-rail{width:80px;min-width:80px;gap:0;padding:4px 0;background:var(--d-item-container,var(--d-surface));align-items:stretch}
:root .d-navigation.d-navigation-rail>button,:root .d-navigation.d-navigation-rail>a{display:flex;flex-direction:column;align-items:center;justify-content:center;gap:4px;min-height:64px;padding:4px 0;font-size:12px;line-height:16px;font-weight:500;letter-spacing:.5px;border:0;border-radius:0;background:transparent;color:var(--d-item-content,var(--d-on-surface-variant))}
:root .d-navigation-rail>[aria-current=page]{border:0;background:transparent;color:var(--d-item-selected-content,var(--d-on-secondary-container))}
.d-navigation-rail>button>span[aria-hidden=true],.d-navigation-rail>a>span[aria-hidden=true]{display:grid;place-items:center;width:max-content;min-width:44px;height:32px;padding-inline:16px;font-size:16px;line-height:24px;border-radius:999px}
.d-navigation-rail>[aria-current=page]>span[aria-hidden=true]{background:var(--d-item-selected-container,var(--d-secondary-container))}
.d-wide-navigation-rail:not(.d-wide-navigation-rail-expanded){width:96px;flex-basis:96px;align-items:stretch;gap:4px;padding-block:44px}
.d-wide-navigation-rail-item-compact{flex-direction:column;gap:4px;min-height:64px;padding:4px 0;font-size:14px;line-height:20px;border-radius:0;background:transparent}
.d-wide-navigation-rail-item-compact[aria-current=page]{background:transparent}
.d-rail-icon{display:grid;place-items:center;width:56px;height:32px;border-radius:999px}
.d-wide-navigation-rail-item-compact[aria-current=page] .d-rail-icon{background:var(--d-item-selected-container,var(--d-secondary-container))}
:root .d-toggle input:focus-visible,:root .d-slider:focus-visible{outline:none}
.d-toggle:has(input:enabled:focus-visible),.d-slider-surface:has(input:focus-visible),.d-range:has(input:focus-visible){outline:3px solid var(--d-primary);outline-offset:3px}
.d-slider:focus-visible::-webkit-slider-thumb{width:2px}.d-slider:focus-visible::-moz-range-thumb{width:2px}
.d-slider-vertical:focus-visible::-webkit-slider-thumb{width:44px;height:2px}.d-slider-vertical:focus-visible::-moz-range-thumb{width:44px;height:2px}
@media(forced-colors:active){.d-toggle:has(input:enabled:focus-visible),.d-slider-surface:has(input:focus-visible),.d-range:has(input:focus-visible){outline:2px solid Highlight}}
.d-date-picker,.d-date-range-picker{padding:0;gap:0;border-radius:0;background:var(--d-surface-container-high);color:var(--d-on-surface-variant)}
.d-date-picker-heading{display:flex;flex-direction:column;justify-content:space-between;min-height:120px;padding:16px 24px;border-bottom:1px solid var(--d-outline-variant)}
.d-date-picker-title{font-size:var(--d-type-label-large-size);line-height:var(--d-type-label-large-line-height);font-weight:var(--d-type-label-large-weight)}
.d-date-picker-headline{display:flex;align-items:center;justify-content:space-between;gap:12px;font-size:var(--d-type-headline-large-size);line-height:var(--d-type-headline-large-line-height)}
.d-date-range-picker .d-date-picker-headline{font-size:var(--d-type-title-large-size);line-height:var(--d-type-title-large-line-height)}
.d-date-picker-headline>button{position:relative;flex:none;width:48px;height:48px;border:0;border-radius:50%;background:transparent;color:inherit}
.d-date-picker-headline>button::before{content:"";position:absolute;inset:12px;background:currentColor;mask:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='white' d='M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zm17.71-10.2a.996.996 0 000-1.41l-2.34-2.34a.996.996 0 00-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z'/%3E%3C/svg%3E") center/contain no-repeat}
.d-date-picker-headline>button[data-input-mode=true]::before{mask:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='white' d='M19 3h-1V1h-2v2H8V1H6v2H5a2 2 0 00-2 2v15a2 2 0 002 2h14a2 2 0 002-2V5a2 2 0 00-2-2zm0 17H5V8h14zM7 10h5v5H7z'/%3E%3C/svg%3E") center/contain no-repeat}
.d-calendar-header{min-height:56px;padding:3px 12px;justify-content:flex-end;gap:0;font-size:var(--d-type-label-large-size);line-height:var(--d-type-label-large-line-height);font-weight:var(--d-type-label-large-weight)}
.d-calendar-header>span{order:0;margin-inline-start:12px;margin-inline-end:auto}.d-calendar-header>button:first-child{order:1}.d-calendar-header>button:last-child{order:2}
.d-calendar-header>button{width:48px;height:48px;border:0;border-radius:50%;background:transparent;color:inherit;font-size:24px}
.d-calendar-grid{padding:0 12px 16px;gap:0}.d-calendar-week{gap:0;min-height:48px;align-items:center;grid-template-columns:repeat(7,minmax(40px,1fr))}
.d-calendar-week>[role=columnheader]{font-size:var(--d-type-body-large-size);line-height:var(--d-type-body-large-line-height);padding:0}
.d-calendar-week>[role=gridcell]{display:grid;place-items:center;height:40px}
.d-calendar-day{width:40px;height:40px;min-width:40px;min-height:40px;padding:0;font-size:var(--d-type-body-large-size);line-height:var(--d-type-body-large-line-height)}
.d-calendar-day[aria-current=date]:not([aria-pressed=true]){border:1px solid var(--d-primary);color:var(--d-primary)}
.d-time-picker{padding:0;gap:24px;width:max-content;max-width:100%}
.d-time-value,.d-time-input-fields{display:flex;align-items:flex-start;justify-content:center;gap:0}
:root .d-time-value>button,:root .d-time-number>input{width:96px;min-width:0;height:80px;padding:0;border:0;border-radius:8px;background:var(--d-surface-container-highest);color:var(--d-on-surface);font-size:var(--d-type-display-large-size);line-height:var(--d-type-display-large-line-height);text-align:center}
.d-time-value>button[aria-pressed=true],:root .d-time-number>input:focus{background:var(--d-primary-container);color:var(--d-on-primary-container)}
.d-time-separator{display:grid;place-items:center;width:24px;height:80px;font-size:57px;line-height:64px}
.d-time-number{display:flex;flex-direction:column;gap:8px}.d-time-number>span{padding-inline-start:16px;font-size:var(--d-type-body-small-size);line-height:var(--d-type-body-small-line-height)}
.d-time-dial{gap:24px}.d-time-dial-controls{width:256px;height:256px;background:var(--d-surface-container-highest)}
.d-time-dial-controls::before{content:"";position:absolute;bottom:50%;left:calc(50% - 1px);width:2px;height:var(--d-clock-radius);background:var(--d-primary);transform-origin:50% 100%;transform:rotate(var(--d-clock-angle));pointer-events:none}
.d-time-dial-controls::after{content:"";position:absolute;left:calc(50% - 4px);top:calc(50% - 4px);width:8px;height:8px;border-radius:50%;background:var(--d-primary);pointer-events:none}
:root .d-time-dial-value{width:48px;height:48px;margin:-24px;color:var(--d-on-surface);font-size:var(--d-type-body-large-size);line-height:var(--d-type-body-large-line-height)}:root .d-time-dial-value[aria-current=true]{color:var(--d-on-primary)}
.d-time-period{display:flex;flex-direction:column;width:52px;height:80px;margin-inline-start:12px;border:1px solid var(--d-outline);border-radius:8px;overflow:hidden}
.d-time-period>button{flex:1;min-height:0;padding:0;border:0;background:transparent;color:var(--d-on-surface-variant);font-size:var(--d-type-title-medium-size);line-height:var(--d-type-title-medium-line-height);font-weight:var(--d-type-title-medium-weight)}
.d-time-period>button+button{border-top:1px solid var(--d-outline)}.d-time-period>button[aria-pressed=true]{background:var(--d-tertiary-container);color:var(--d-on-tertiary-container)}
.d-slider-surface{height:auto;min-height:48px}.d-slider-surface-vertical{min-height:160px}.d-slider-surface>.d-slider{height:100%}
.d-component-label:has(>.d-component-label-content>.d-slider-surface:not(.d-slider-surface-vertical)){width:100%}
.d-scroll-field-viewport{box-sizing:border-box}
.d-button-hit-target{height:max-content;display:inline-flex;align-items:center;justify-content:center;min-width:48px;min-height:48px}
.d-button-hit-target>.d-button{flex:1}.d-button-hit-target>.d-button::after{content:"";position:absolute;inset-inline:0;top:50%;height:100%;min-height:48px;transform:translateY(-50%)}
:root .d-time-number>input[aria-invalid=true]{background:var(--d-error-container);color:var(--d-on-error-container)}
.d-segments{flex-wrap:nowrap;max-width:100%}.d-segments-equal{display:grid;grid-auto-flow:column;grid-auto-columns:1fr;width:max-content}
.d-segment{min-height:var(--d-item-min-interactive,48px)}.d-segment-surface{justify-content:center;gap:var(--d-item-gap,8px)}
.d-segment-checkmark,.d-segment-icon-space{display:inline-flex;flex:none;align-items:center;justify-content:center;width:18px;height:18px}
.d-segment-icon-space+.d-segment-label{transform:translateX(-13px)}
.d-segment-checkmark:empty{background:currentColor;mask:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath fill='white' d='M9 16.17 4.83 12 3.41 13.41 9 19 21 7 19.59 5.59z'/%3E%3C/svg%3E") center/contain no-repeat}
.d-multi-segments{display:grid;grid-auto-flow:column;grid-auto-columns:1fr;width:max-content;max-width:100%;gap:0}
.d-multi-segment-hit-target{display:flex;align-items:center;min-height:var(--d-item-min-interactive,48px)}
.d-multi-segment-hit-target>.d-multi-segment{position:relative;display:flex;align-items:center;justify-content:center;gap:var(--d-item-gap,8px);flex:1;border-radius:0}
.d-multi-segment-hit-target+.d-multi-segment-hit-target{margin-inline-start:calc(-1 * var(--d-item-border-width,1px))}
.d-multi-segment-hit-target:first-child>.d-multi-segment{border-start-start-radius:var(--d-item-radius,999px);border-end-start-radius:var(--d-item-radius,999px)}
.d-multi-segment-hit-target:last-child>.d-multi-segment{border-start-end-radius:var(--d-item-radius,999px);border-end-end-radius:var(--d-item-radius,999px)}
.d-multi-segment::after{content:"";position:absolute;inset-inline:0;top:50%;height:100%;min-height:var(--d-item-min-interactive,48px);transform:translateY(-50%)}
.d-chip,.d-chip-surface{gap:var(--d-item-gap,8px)}.d-chip-surface{display:inline-flex;align-items:center;justify-content:center;box-sizing:border-box;min-width:0;max-width:100%}
.d-chip-elevated:not(.d-item-hittarget){box-shadow:0 1px 3px rgb(0 0 0 / .24)}.d-chip-elevated.d-item-hittarget .d-chip-surface{box-shadow:0 1px 3px rgb(0 0 0 / .24)}
:root .d-navigation:not(.d-navigation-links)>button>span[aria-hidden=true]:first-child,:root .d-navigation:not(.d-navigation-links)>a>span[aria-hidden=true]:first-child{width:max-content;min-width:44px;padding-inline:16px;font-size:16px;line-height:24px}
:root .d-navigation:not(.d-navigation-links)>button,:root .d-navigation:not(.d-navigation-links)>a{font-weight:500;letter-spacing:.5px}
.d-bottom-app-bar{width:100%;border-radius:0}
.d-persistent-bottom-sheet{display:flex;flex-direction:column;flex:none;overflow:hidden;padding:0;border-radius:28px 28px 0 0;background:var(--d-surface-container-low);box-shadow:0 1px 2px #0003}
.d-persistent-bottom-sheet>.d-bottom-sheet-handle{display:grid;place-items:center;flex:none;align-self:center;width:48px;min-height:48px;padding:0;border:0;background:transparent;color:var(--d-on-surface-variant)}
.d-bottom-sheet-grip{display:block;width:32px;height:4px;border-radius:999px;background:currentColor}
.d-snackbar-message{width:100%}
.d-avatar{color:var(--d-on-surface-variant);font-size:var(--d-type-label-large-size);line-height:var(--d-type-label-large-line-height);font-weight:var(--d-type-label-large-weight);letter-spacing:var(--d-type-label-large-letter-spacing)}
:root .d-fab{min-width:0;min-height:0}.d-button-hit-target>.d-fab{flex:none}.d-fab-menu-toggle{width:56px;height:56px}
.d-button-toggle{padding:10px 16px}.d-button-toggle.d-button-elevated:not([aria-pressed=true]){color:var(--d-primary)}
.d-floating-toolbar{padding:8px;border-radius:999px;box-shadow:none;gap:0}
.d-carousel-track{height:100%;align-items:center}.d-carousel-item{display:flex;align-items:center;height:100%}.d-carousel-item>.d-card{flex:none}
.d-carousel-controls{position:absolute;inset-inline:0;bottom:8px;opacity:0;transition:opacity 120ms ease}.d-carousel:hover>.d-carousel-controls,.d-carousel:focus-within>.d-carousel-controls{opacity:1}
.d-loading-contained{padding:0}.d-loading-contained .d-loading-determinate,.d-loading-contained .d-loading-morph-contained{fill:var(--d-progress-color,var(--d-on-primary-container))}
.d-progress-wavy-linear{display:flex}
:root .d-icon-button{position:relative;width:40px;height:40px;min-width:40px;min-height:40px;padding:0}
.d-button-hit-target>.d-icon-button::after,.d-button-hit-target>.d-fab::after{content:"";position:absolute;left:50%;right:auto;top:50%;width:max(100%,48px);height:max(100%,48px);transform:translate(-50%,-50%)}
.d-split-button{min-height:48px;align-items:center;gap:2px}.d-split-button>.d-button:first-child{padding-inline:16px;border-start-end-radius:8px;border-end-end-radius:8px}.d-split-button>.d-button:last-child{width:48px;min-width:48px;padding-inline:0;border-start-start-radius:8px;border-end-start-radius:8px;border-inline-start:0}
.d-wide-navigation-rail-item{font-size:var(--d-type-label-large-size);line-height:var(--d-type-label-large-line-height);font-weight:var(--d-type-label-large-weight);letter-spacing:var(--d-type-label-large-letter-spacing)}
.d-app-bar-largeflexible .d-app-bar-title{font-size:var(--d-type-display-small-size);line-height:var(--d-type-display-small-line-height)}
.d-app-bar-centeraligned .d-app-bar-heading{align-items:center}.d-app-bar-centeraligned .d-app-bar-title{text-align:center}
.d-app-bar-subtitle{font-size:var(--d-type-body-small-size);line-height:var(--d-type-body-small-line-height)}
.d-app-bar-mediumflexible .d-app-bar-subtitle{font-size:var(--d-type-label-large-size);line-height:var(--d-type-label-large-line-height)}
.d-app-bar-largeflexible .d-app-bar-subtitle{font-size:var(--d-type-title-medium-size);line-height:var(--d-type-title-medium-line-height)}
.d-app-bar-navigation,.d-app-bar-actions{display:flex;align-items:center}.d-app-bar-navigation:empty,.d-app-bar-actions:empty{display:none}
.d-app-bar-navigation:not(:empty){margin-inline-start:-12px}.d-app-bar-actions:not(:empty){margin-inline-end:-12px}
.d-app-bar-expanded{position:relative}
.d-app-bar-expanded>.d-app-bar-navigation{position:absolute;top:8px;inset-inline-start:4px;margin:0}
.d-app-bar-expanded>.d-app-bar-actions{position:absolute;top:8px;inset-inline-end:4px;margin:0}
.d-app-bar-subtitle{opacity:1}
.d-app-bar{width:100%}.d-app-bar-copy{display:flex;flex-direction:column;gap:0;min-width:0;transform:translateY(calc(-1 * var(--d-app-bar-baseline-gap,0px)))}
.d-app-bar-expanded{display:grid;grid-template-columns:minmax(0,1fr);grid-template-rows:64px minmax(calc(var(--d-app-bar-height) - 64px),max-content);padding:0 8px 0 16px;gap:0}
.d-app-bar-expanded>.d-app-bar-heading{position:static;grid-row:2;grid-column:1;justify-content:flex-end;align-self:stretch;min-width:0;gap:0}

.d-field{min-width:min(280px,100%);max-width:100%}
.d-field>.d-helper,.d-field>.d-error{padding-inline:16px;font-size:var(--d-type-body-small-size);line-height:var(--d-type-body-small-line-height);letter-spacing:var(--d-type-body-small-letter-spacing);color:var(--d-on-surface-variant)}
.d-field>.d-error{color:var(--d-error)}
.d-search-bar{width:min(360px,100%);min-width:0}.d-autocomplete{width:100%;min-width:0}
.d-search-control{border-width:var(--d-input-outline-width,0px);border-radius:999px;background:var(--d-input-container,var(--d-surface-container-high))}
.d-search-control:focus-within{border-width:var(--d-input-outline-focused-width,0px);background:var(--d-input-container-focused,var(--d-input-container,var(--d-surface-container-high)))}
.d-search-control input{font:inherit;line-height:24px;letter-spacing:.5px}
.d-autocomplete{padding-top:8px;gap:0}
.d-autocomplete>label{position:absolute;z-index:1;inset-inline-start:16px;top:24px;font-size:var(--d-input-label-size,16px);line-height:24px;color:var(--d-on-surface-variant);pointer-events:none}
.d-autocomplete:focus-within>label,.d-autocomplete[data-filled=true]>label{top:0;padding-inline:4px;margin-inline-start:-4px;font-size:12px;line-height:16px;background:var(--d-input-container,var(--d-surface))}
.d-autocomplete:focus-within>label{color:var(--d-input-outline-focused,var(--d-primary))}
.d-autocomplete-control{gap:12px}
.d-autocomplete-control input{font:inherit;line-height:24px;letter-spacing:.5px}
.d-autocomplete-indicator{flex:none;width:0;height:0;border-inline:4px solid transparent;border-top:5px solid currentColor;margin-inline:4px}
.d-autocomplete[data-variant=filled] .d-autocomplete-control{border-width:0 0 var(--d-input-outline-width,1px);border-radius:4px 4px 0 0;background:var(--d-input-container,var(--d-surface-container-highest))}
.d-autocomplete[data-variant=filled]:focus-within>label,.d-autocomplete[data-variant=filled][data-filled=true]>label{top:12px;background:transparent}
.d-autocomplete[data-variant=filled]:focus-within input,.d-autocomplete[data-variant=filled][data-filled=true] input{padding-top:16px}
.d-toggle input[role=switch]:active:not(:disabled){background-size:28px 28px;background-position:0 center;transition:none}
.d-toggle input[role=switch]:checked:active:not(:disabled){background-position:100% center}
.d-toggle:has(input:enabled):hover:active::before{opacity:.172}
"""
