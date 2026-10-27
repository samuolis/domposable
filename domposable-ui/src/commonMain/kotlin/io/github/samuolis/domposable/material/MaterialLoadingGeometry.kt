/*
 * Copyright 2024 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.
 */
package io.github.samuolis.domposable.material

import androidx.collection.FloatFloatPair
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private const val PETALS = 10
private const val SVG_SIZE = 100f
private const val ACTIVE_SCALE = 38f / 48f
private const val ROUNDING = 0.053f
private const val CENTER = 0.5f
private const val INNER_X = 0.193f
private const val INNER_Y = 0.277f
private const val OUTER_X = 0.176f
private const val OUTER_Y = 0.055f

// Material 1.12's circle/soft-burst sequence, using its existing AndroidX polygon morph engine.
internal val MaterialLoadingPolygons: List<RoundedPolygon> by lazy {
    val angle = PI.toFloat() / PETALS
    val circle =
        RoundedPolygon.circle(PETALS).normalized().transformed { x, y ->
            FloatFloatPair(x * cos(angle) - y * sin(angle), x * sin(angle) + y * cos(angle))
        }
    val burst =
        RoundedPolygon(
            vertices =
                FloatArray(PETALS * 4) { coordinate ->
                    val vertex = coordinate / 2
                    val rotation = (vertex / 2) * 2f * PI.toFloat() / PETALS
                    val x = (if (vertex % 2 == 0) INNER_X else OUTER_X) - CENTER
                    val y = (if (vertex % 2 == 0) INNER_Y else OUTER_Y) - CENTER
                    if (coordinate % 2 == 0) {
                        x * cos(rotation) - y * sin(rotation) + CENTER
                    } else {
                        x * sin(rotation) + y * cos(rotation) + CENTER
                    }
                },
            rounding = CornerRounding(ROUNDING),
            centerX = CENTER,
            centerY = CENTER,
        ).normalized()
    listOf(circle, burst)
}

private val LoadingMorph by lazy {
    Morph(MaterialLoadingPolygons[0].normalized(), MaterialLoadingPolygons[1].normalized())
}
private val LoadingScale by lazy {
    MaterialLoadingPolygons.fold(1f) { scale, polygon ->
        val bounds = polygon.calculateBounds()
        val maxBounds = polygon.calculateMaxBounds()
        min(
            scale,
            max(
                (bounds[2] - bounds[0]) / (maxBounds[2] - maxBounds[0]),
                (bounds[3] - bounds[1]) / (maxBounds[3] - maxBounds[1]),
            ),
        )
    } * ACTIVE_SCALE * SVG_SIZE
}

internal fun determinateLoadingPath(progress: Float): String {
    val cubics = LoadingMorph.asCubics(progress.coerceIn(0f, 1f))
    val xs = cubics.flatMap { listOf(it.anchor0X, it.control0X, it.control1X, it.anchor1X) }
    val ys = cubics.flatMap { listOf(it.anchor0Y, it.control0Y, it.control1Y, it.anchor1Y) }
    val centerX = (xs.min() + xs.max()) / 2f
    val centerY = (ys.min() + ys.max()) / 2f

    fun point(
        x: Float,
        y: Float,
    ) = "${(x - centerX) * LoadingScale + SVG_SIZE / 2f},${(y - centerY) * LoadingScale + SVG_SIZE / 2f}"
    return buildString {
        append("M${point(cubics.first().anchor0X, cubics.first().anchor0Y)}")
        cubics.forEach {
            append(
                " C${point(
                    it.control0X,
                    it.control0Y,
                )} ${point(it.control1X, it.control1Y)} ${point(it.anchor1X, it.anchor1Y)}",
            )
        }
        append(" Z")
    }
}
