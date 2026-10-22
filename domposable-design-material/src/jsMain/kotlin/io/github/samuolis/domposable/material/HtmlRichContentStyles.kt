package io.github.samuolis.domposable.material

internal const val HTML_RICH_CONTENT_CSS = """
.d-rich-html{min-width:0;line-height:1.5}
.d-rich-html p{margin:0 0 16px}
.d-rich-html h1,.d-rich-html h2,.d-rich-html h3{margin:0 0 12px}
.d-rich-html h1{font-size:var(--d-type-headline-small-size);line-height:var(--d-type-headline-small-line-height);font-weight:var(--d-type-headline-small-weight)}
.d-rich-html h2,.d-rich-html h3{font-size:var(--d-type-title-medium-size);line-height:var(--d-type-title-medium-line-height);font-weight:var(--d-type-title-medium-weight)}
.d-rich-html ul,.d-rich-html ol{padding-inline-start:24px;margin:0 0 16px}
.d-rich-html li{padding-inline-start:4px;margin-block:4px}
.d-rich-html blockquote{margin:0 0 16px;padding-inline-start:16px;border-inline-start:3px solid var(--d-outline-variant,var(--d-outline))}
.d-rich-html code{font-family:monospace}
.d-rich-text-toolbar-group{max-width:100%;min-width:0}
.d-rich-text-dialog input:focus-visible{outline:3px solid var(--d-primary);outline-offset:2px}
.d-rich-text-toolbar-icon{display:inline-flex;align-items:center;justify-content:center;min-width:20px;font:inherit}
.d-rich-text-editor{overflow:auto;box-sizing:border-box}
.d-rich-text-editor__surface{min-height:100%;padding:14px 16px;outline:0;white-space:pre-wrap;overflow-wrap:anywhere;font:14px/1.55 var(--d-font,sans-serif);color:inherit;box-sizing:border-box}
.d-rich-text-editor__surface p{margin:0 0 .72em}.d-rich-text-editor__surface p:last-child{margin-bottom:0}
.d-rich-text-editor__surface h1,.d-rich-text-editor__surface h2,.d-rich-text-editor__surface h3{margin:0 0 .45em;line-height:1.25}
.d-rich-text-editor__surface ul,.d-rich-text-editor__surface ol{padding-inline-start:1.5em;margin:.4em 0}
.d-rich-text-editor__surface a{color:var(--d-primary);text-decoration:underline;text-underline-offset:2px}
.d-rich-text-editor__surface img{display:block;max-width:100%;height:auto;border-radius:8px}
.d-rich-text-editor__surface pre{overflow:auto;padding:12px;border-radius:8px;background:var(--d-surface-variant,var(--d-variant))}
.d-rich-text-editor__surface code{font-family:ui-monospace,SFMono-Regular,Consolas,monospace}
.d-rich-text-dialog-scrim{position:fixed;inset:0;z-index:500;display:grid;place-items:center;padding:16px;background:rgb(16 24 32 / .48)}
.d-rich-text-dialog{box-shadow:0 18px 48px rgb(0 0 0 / .24)}
.d-rich-text-dialog input{width:100%;min-height:48px;padding:10px 12px;border:1px solid var(--d-outline-variant,var(--d-outline));border-radius:9px;background:var(--d-background,var(--d-surface));color:var(--d-on-surface);font:inherit;box-sizing:border-box}
"""
