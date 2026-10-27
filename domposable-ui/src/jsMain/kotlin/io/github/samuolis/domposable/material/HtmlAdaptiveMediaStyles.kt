package io.github.samuolis.domposable.material

/** CSS hooks for adaptive pickers, media marquees, collapsible headers, status art, and confetti. */
internal fun htmlAdaptiveMediaStylesCss(): String = ADAPTIVE_MEDIA_CSS

private const val ADAPTIVE_MEDIA_CSS = """
.d-picker-calendar-controls select,.d-picker-calendar-controls input{box-sizing:border-box;min-width:0;min-height:44px;border:1px solid var(--d-outline-variant,var(--d-outline));border-radius:8px;background:var(--d-surface-container,var(--d-surface));color:var(--d-on-surface);font:inherit}
.d-picker-calendar-controls select{flex:1 1 auto}
.d-picker-calendar-controls input{width:76px;flex:none;padding:0 6px}
.d-picker-calendar-grid button[aria-pressed=true]{position:relative;isolation:isolate}
.d-picker-calendar-grid button[aria-pressed=true]::before{position:absolute;z-index:-1;inset-block-start:50%;inset-inline-start:50%;width:min(100%,44px);aspect-ratio:1;border-radius:50%;background:var(--d-primary);content:"";transform:translate(-50%,-50%)}
.d-picker-time{font-size:28px}
.d-picker-time input{box-sizing:border-box;width:92px;max-width:40%;min-width:0;flex:0 1 92px;min-height:56px;text-align:center;border:1px solid var(--d-outline-variant,var(--d-outline));border-radius:12px;background:var(--d-surface-container,var(--d-surface));color:var(--d-on-surface);font:inherit}
.d-picker-time select{box-sizing:border-box;min-width:0;min-height:56px;border:1px solid var(--d-outline-variant,var(--d-outline));border-radius:12px;background:var(--d-surface-container,var(--d-surface));color:var(--d-on-surface);font:inherit;font-size:18px}
.d-media-marquee{overflow:hidden;touch-action:pan-y}.d-media-marquee-item img{display:block}
.d-media-marquee-moving{animation:d-media-marquee var(--d-media-marquee-duration,36s) linear infinite}.d-media-marquee:hover .d-media-marquee-moving,.d-media-marquee:active .d-media-marquee-moving{animation-play-state:paused}@keyframes d-media-marquee{to{transform:translateX(calc(0px - var(--d-media-marquee-distance,50%)))}}@media(prefers-reduced-motion:reduce){.d-media-marquee-moving{animation:none}}
.d-collapsible-layout{height:100%;min-height:0;overflow:hidden}
.d-collapsible-header{flex:0 0 auto;z-index:2;background:var(--d-surface-container,var(--d-surface))}
.d-collapsible-header[data-collapsed="true"]{box-shadow:0 1px 4px color-mix(in srgb,var(--d-outline-variant,var(--d-outline)) 28%,transparent)}
.d-collapsible-content{overscroll-behavior:contain}
.d-status-illustration[data-pulse="true"] .d-status-illustration-halo{animation:d-status-halo-pulse var(--d-status-halo-duration,1s) var(--d-status-halo-easing,cubic-bezier(.4,0,.2,1)) infinite alternate;transform-origin:center}
.d-status-illustration-disc[data-animated="true"]{animation:d-status-disc-enter var(--d-status-disc-enter-duration,600ms) var(--d-status-disc-enter-easing,cubic-bezier(.4,0,.2,1)) both}
@keyframes d-status-halo-pulse{from{transform:scale(1)}to{transform:scale(1.1)}}
@keyframes d-status-disc-enter{from{transform:scale(0)}to{transform:scale(1)}}
@media(prefers-reduced-motion:reduce){.d-status-illustration[data-pulse="true"] .d-status-illustration-halo,.d-status-illustration-disc[data-animated="true"]{animation:none}}
.d-confetti>.d-confetti-piece{animation:d-confetti-fall var(--d-confetti-duration,7s) cubic-bezier(.2,.65,.35,1) var(--d-confetti-delay,0ms) infinite}
@keyframes d-confetti-fall{0%{transform:translate3d(0,-24px,0) rotate(var(--d-confetti-rotation,0deg));opacity:0}12%{opacity:.32}100%{transform:translate3d(0,100cqh,0) rotate(calc(var(--d-confetti-rotation,0deg) + 720deg));opacity:0}}
@media(prefers-reduced-motion:reduce){.d-confetti{display:none!important}}
"""
