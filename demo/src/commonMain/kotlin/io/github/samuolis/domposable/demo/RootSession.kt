package io.github.samuolis.domposable.demo

import kotlin.random.Random

internal class RootSession {
    val instance = Random.nextLong().toString(16)
    var closed = false
        private set

    fun close() {
        closed = true
    }
}
