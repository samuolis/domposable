package io.github.samuolis.domposable.material

/** Generic hooks for the app header, skip link, footer, modal drawer, and breadcrumbs. */
internal const val DOMPOSABLE_HTML_SHELL_SLOT_CSS = """
.d-frosted-surface{background:var(--d-frosted-solid-color,var(--d-surface))}
@supports ((backdrop-filter:blur(1px)) or (-webkit-backdrop-filter:blur(1px))){.d-frosted-surface{background:var(--d-frosted-surface-color,color-mix(in srgb,var(--d-surface) 63%,transparent));backdrop-filter:blur(20px);-webkit-backdrop-filter:blur(20px)}}
@media(prefers-reduced-transparency:reduce),(forced-colors:active){.d-frosted-surface{background:var(--d-frosted-solid-color,var(--d-surface));backdrop-filter:none;-webkit-backdrop-filter:none}}
.d-skip-link{position:absolute;top:8px;left:16px;transform:translateY(-200%);z-index:50;padding:12px;border-radius:12px;background:var(--d-surface);color:var(--d-primary)}
.d-skip-link:focus{left:16px;top:8px;transform:none;z-index:50}
.d-app-header{position:sticky;top:0;z-index:var(--d-app-header-z-index,20);padding-top:env(safe-area-inset-top,0px);padding-inline:max(0px,env(safe-area-inset-left,0px)) max(0px,env(safe-area-inset-right,0px));background:var(--d-app-header-surface,var(--d-surface));border-bottom:1px solid color-mix(in srgb,var(--d-outline-variant,var(--d-outline)) 24%,transparent)}
@supports ((backdrop-filter:blur(1px)) or (-webkit-backdrop-filter:blur(1px))){.d-app-header{background:color-mix(in srgb,var(--d-app-header-surface,var(--d-surface)) var(--d-app-header-opacity,88%),transparent);backdrop-filter:blur(var(--d-app-header-blur,28px)) saturate(var(--d-app-header-saturation,140%));-webkit-backdrop-filter:blur(var(--d-app-header-blur,28px)) saturate(var(--d-app-header-saturation,140%))}}
@media(prefers-reduced-transparency:reduce){.d-app-header{background:var(--d-app-header-surface,var(--d-surface));backdrop-filter:none;-webkit-backdrop-filter:none}}
.d-app-header :is(a,button),.d-navigation-panel :is(a,button){-webkit-tap-highlight-color:transparent}
.d-web-footer{padding-bottom:env(safe-area-inset-bottom,0px);background:var(--d-surface-container,var(--d-surface));color:var(--d-on-surface)}
.d-app-header :is(a,button):focus-visible,.d-web-footer :is(a,button):focus-visible,.d-skip-link:focus-visible{outline:2px solid var(--d-primary);outline-offset:3px}
@media(forced-colors:active){.d-app-header{background:Canvas;border:1px solid CanvasText;backdrop-filter:none;-webkit-backdrop-filter:none}.d-app-header :is(a,button):focus-visible,.d-web-footer :is(a,button):focus-visible,.d-skip-link:focus-visible{outline-color:Highlight}}
.d-navigation-panel-backdrop{position:fixed;inset:0;z-index:1000;display:flex;align-items:stretch;background:color-mix(in srgb,var(--d-on-surface) 28%,transparent);overscroll-behavior:contain}
.d-navigation-panel{--d-navigation-panel-width:384px;--d-navigation-panel-gutter:32px;--d-navigation-panel-inset-start:max(16px,env(safe-area-inset-left,0px));--d-navigation-panel-inset-end:max(16px,env(safe-area-inset-right,0px));flex:0 0 min(var(--d-navigation-panel-width),calc(100vw - var(--d-navigation-panel-gutter)));width:min(var(--d-navigation-panel-width),calc(100vw - var(--d-navigation-panel-gutter)));max-width:100%;height:100%;box-sizing:border-box;overflow-x:hidden;padding-block:max(16px,env(safe-area-inset-top,0px)) max(16px,env(safe-area-inset-bottom,0px));border-inline-end:1px solid var(--d-outline-variant,var(--d-outline));box-shadow:8px 0 32px color-mix(in srgb,var(--d-on-surface) 16%,transparent);animation:d-navigation-panel-enter var(--d-navigation-panel-enter-duration,180ms) ease-out both}
.d-navigation-panel>*{box-sizing:border-box;overscroll-behavior:contain}
.d-navigation-panel :is(a,button){-webkit-tap-highlight-color:transparent}
@keyframes d-navigation-panel-enter{from{opacity:.7;transform:translateX(-12px)}to{opacity:1;transform:translateX(0)}}
@media(prefers-reduced-motion:reduce){.d-navigation-panel{animation:none}}
@media(forced-colors:active){.d-navigation-panel-backdrop{background:rgb(0 0 0 / .5)}.d-navigation-panel{background:Canvas!important;color:CanvasText!important;border:1px solid CanvasText;box-shadow:none}.d-navigation-panel-close{border:1px solid CanvasText!important;background:Canvas!important;color:CanvasText!important}}
.d-breadcrumbs{position:static;box-sizing:border-box;width:100%;max-width:var(--d-breadcrumbs-max-width,1200px);margin-inline:auto;padding:0 var(--d-breadcrumbs-gutter,16px) 12px;font-size:13px;line-height:18px}
.d-breadcrumbs ol{display:flex;align-items:center;flex-wrap:wrap;gap:6px 10px;padding:0;margin:0;list-style:none}
.d-breadcrumbs li{display:inline-flex;align-items:center;gap:10px;min-width:0}
.d-breadcrumbs li+li::before{content:"/";color:var(--d-on-surface-variant)}
.d-breadcrumbs a{display:inline-flex;align-items:center;min-height:24px;color:var(--d-primary);text-underline-offset:3px}
.d-breadcrumbs [aria-current]{color:var(--d-on-surface-variant);overflow-wrap:anywhere}
@media(max-width:600px){.d-breadcrumbs{padding:0 var(--d-breadcrumbs-gutter,16px) 8px;font-size:12px}.d-breadcrumbs ol{gap:4px 8px}.d-breadcrumbs li{gap:8px}}

.d-navigation-panel .d-dropdown-menu-item{min-height:48px!important;padding:0 14px!important;border-radius:12px!important;font:inherit}
.d-page-heading .d-page-actions{flex:0 0 auto}
.d-page-heading .d-page-actions>*{flex:0 0 auto;white-space:nowrap}
.d-page-breadcrumbs{color:var(--d-on-surface-variant);font-size:13px;line-height:18px}
.d-page-breadcrumbs>*{min-width:0}
.d-action-menu{position:relative}
.d-action-menu .d-dropdown-menu-item{min-height:44px;white-space:normal}
@media(prefers-reduced-motion:reduce){.d-app-header{scroll-behavior:auto}}
@media(forced-colors:active){.d-navigation-panel :is(a,button):focus-visible{outline-color:Highlight}}
"""
