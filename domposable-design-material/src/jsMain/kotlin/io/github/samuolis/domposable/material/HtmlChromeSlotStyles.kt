package io.github.samuolis.domposable.material

/** CSS hooks for sticky content slots, contextual navigation, and floating action controls. */
internal const val DOMPOSABLE_HTML_CHROME_SLOT_CSS = """
.d-bottom-bar{position:sticky;bottom:0;z-index:12;width:100%;min-width:0;flex:none;box-sizing:border-box;isolation:isolate;padding-bottom:env(safe-area-inset-bottom);background:var(--d-background)}
.d-bottom-bar-floating{padding-bottom:calc(16px + env(safe-area-inset-bottom))}
.d-bottom-bar-host-floating{position:static;background:transparent}
.d-bottom-bar-anchor{position:sticky!important;bottom:0;z-index:12;flex:none!important}
.d-bottom-bar-anchor .d-bottom-bar-host{position:static}
.d-contextual-action-copy{display:inline-flex;align-items:center;gap:8px}
.d-contextual-frosted{backdrop-filter:blur(18px);box-shadow:0 6px 24px rgb(0 0 0 / 14%)}
.d-contextual-navigation{scrollbar-width:none}
.d-contextual-navigation:not([data-presentation="underline-tabs"]){width:fit-content;max-width:100%;overflow-x:auto}
.d-contextual-navigation::-webkit-scrollbar{display:none}
.d-floating-action-toolbar{box-shadow:0 2px 6px color-mix(in srgb,var(--d-shadow,var(--d-scrim,#000)) 14%,transparent);backdrop-filter:blur(14px);-webkit-backdrop-filter:blur(14px)}
.d-floating-action-toolbar-item{flex:none}
.d-centered-content{text-align:center}
@media(min-width:601px){.d-bottom-bar-desktop-inline:not(.d-contextual-navigation-host .d-bottom-bar-desktop-inline){bottom:0;width:100%;max-width:var(--d-bottom-bar-inline-max-width,1200px);margin:16px auto 0;padding:0 0 env(safe-area-inset-bottom);border:0;border-radius:0;background:var(--d-background);box-shadow:none;backdrop-filter:none;-webkit-backdrop-filter:none}
.d-bottom-bar-anchor:not(.d-contextual-navigation-host):not(:has(.d-contextual-navigation-host)){bottom:0!important}
.d-bottom-bar-desktop-inline:not(.d-contextual-navigation-host .d-bottom-bar-desktop-inline) .d-layout-column:not(.d-bottom-bar-desktop-inline .d-layout-column .d-layout-column)>.d-divider,.d-bottom-bar-desktop-inline:not(.d-contextual-navigation-host .d-bottom-bar-desktop-inline) .d-layout-column:not(.d-bottom-bar-desktop-inline .d-layout-column .d-layout-column)>div>.d-divider,.d-divider:has(+.d-bottom-bar-desktop-inline:not(.d-contextual-navigation-host .d-bottom-bar-desktop-inline)){display:none}
.d-contextual-navigation-host .d-bottom-bar-desktop-inline{bottom:auto;width:100%;max-width:none;margin:0;padding:0;border:0;border-radius:0;background:transparent;box-shadow:none;backdrop-filter:none;-webkit-backdrop-filter:none}
.d-contextual-navigation-host.d-bottom-bar-anchor,.d-contextual-navigation-host .d-bottom-bar-anchor{bottom:0!important}}
"""
