package io.github.samuolis.domposable.adaptive

import androidx.compose.runtime.*
import kotlinx.browser.window

@Composable private fun mediaPreference(query: String): Boolean {
    val media = remember(query) { window.matchMedia(query) }
    var matches by remember(query) { mutableStateOf(media.matches) }
    DisposableEffect(media) {
        val listener: (org.w3c.dom.events.Event) -> Unit = { matches = media.matches }
        media.addEventListener("change", listener)
        onDispose { media.removeEventListener("change", listener) }
    }
    return matches
}
@Composable internal actual fun reducedMotionPreference(): Boolean = mediaPreference("(prefers-reduced-motion: reduce)")
@Composable internal actual fun precisePointerPreference(): Boolean = mediaPreference("(pointer: fine)")
