@file:JsModule("@rive-app/canvas")
@file:JsNonModule

package io.github.samuolis.domposable.rive

internal external class Rive(
    options: dynamic,
) {
    fun resizeDrawingSurfaceToCanvas()

    fun cleanup()
}
