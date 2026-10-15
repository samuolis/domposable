package io.github.samuolis.domposable.demo

import androidx.compose.runtime.saveable.Saver

// Entry restoration uses portable values on every target, including the browser.
internal inline fun <reified T : Enum<T>> enumSaver(): Saver<T, String> =
    Saver(save = { it.name }, restore = { enumValueOf<T>(it) })
