package io.github.samuolis.domposable.foundation

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

/** A logical, density-independent length shared by native and HTML renderers. */
@Immutable
@JvmInline
value class DomposableLength(
    val value: Float,
) {
    init {
        require(value.isFinite() && value >= 0f) { "Length must be finite and non-negative" }
    }
}

val Int.dp: DomposableLength get() = DomposableLength(toFloat())
val Float.dp: DomposableLength get() = DomposableLength(this)

@Immutable
@JvmInline
value class DomposableColor(
    val argb: Long,
) {
    init {
        require(argb in 0..0xFFFFFFFFL)
    }

    fun css(): String {
        val red = (argb shr 16) and 255
        val green = (argb shr 8) and 255
        val blue = argb and 255
        return "rgba($red,$green,$blue,${((argb shr 24) and 255) / 255.0})"
    }
}

@Immutable
data class DomposableInsets(
    val top: DomposableLength = 0.dp,
    val end: DomposableLength = 0.dp,
    val bottom: DomposableLength = 0.dp,
    val start: DomposableLength = 0.dp,
) {
    constructor(all: DomposableLength) : this(all, all, all, all)
}

@Immutable
data class DomposableSize(
    val width: Float,
    val height: Float,
)

@Immutable
data class DomposableTypography(
    val fontFamily: String = "system-ui",
    val bodySize: Float = 16f,
    val titleSize: Float = 24f,
    val labelSize: Float = 14f,
    val lineHeight: Float = 1.5f,
)

@Immutable
data class DomposableColors(
    val primary: DomposableColor = DomposableColor(0xFF4059AD),
    val onPrimary: DomposableColor = DomposableColor(0xFFFFFFFF),
    val surface: DomposableColor = DomposableColor(0xFFF9FAFF),
    val onSurface: DomposableColor = DomposableColor(0xFF1C2030),
    val surfaceVariant: DomposableColor = DomposableColor(0xFFECEFF8),
    val outline: DomposableColor = DomposableColor(0xFF74798C),
    val error: DomposableColor = DomposableColor(0xFFBA1A1A),
)

@Immutable
data class DomposableSpacing(
    val small: DomposableLength = 8.dp,
    val medium: DomposableLength = 16.dp,
    val large: DomposableLength = 24.dp,
)

@Immutable
data class DomposableTokens(
    val colors: DomposableColors = DomposableColors(),
    val typography: DomposableTypography = DomposableTypography(),
    val spacing: DomposableSpacing = DomposableSpacing(),
    val radius: DomposableLength = 12.dp,
    val elevation: DomposableLength = 2.dp,
    val motionDurationMillis: Int = 180,
) {
    init {
        require(motionDurationMillis >= 0)
    }
}

enum class DomposableImageScale { Fit, Crop, Fill }

enum class DomposableTextStyle { Body, Title, Label }

enum class DomposableAlignment { Start, Center, End }

enum class DomposableOrientation { Vertical, Horizontal }

enum class DomposableButtonStyle { Filled, Outlined, Text }

enum class DomposableInputPurpose { Text, Email, Password, Number, Search, Date, Time }

/** Declaration analysis only: generators never rewrite a composable's body. */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class DomposableScreen(
    val route: KClass<*>,
    val viewModel: Boolean = false,
)

/** Generates a typed renderer contract and forwarding API for this props declaration. */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class DomposableComponent(
    val name: String,
)

sealed interface DomposableScreenState<out T> {
    data object Loading : DomposableScreenState<Nothing>

    data class Error(
        val message: String,
        val retry: (() -> Unit)? = null,
    ) : DomposableScreenState<Nothing>

    data class Content<T>(
        val value: T,
    ) : DomposableScreenState<T>
}
