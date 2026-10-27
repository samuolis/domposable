package io.github.samuolis.domposable.material

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.github.samuolis.domposable.foundation.DomposableColors

internal fun DomposableColors.nativeMaterialColorScheme(): ColorScheme =
    nativeColorScheme(
        lightColorScheme(
            primary = Color(primary.argb),
            onPrimary = Color(onPrimary.argb),
            surface = Color(surface.argb),
            onSurface = Color(onSurface.argb),
            surfaceVariant = Color(surfaceVariant.argb),
            outline = Color(outline.argb),
            error = Color(error.argb),
            background = Color(surface.argb),
            surfaceContainer = Color(surfaceVariant.argb),
            surfaceContainerHighest = Color(surfaceVariant.argb),
            secondaryContainer = Color(surfaceVariant.argb),
            onSecondaryContainer = Color(onSurface.argb),
        ),
    )
