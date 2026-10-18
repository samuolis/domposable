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

    /** Hex CSS form for product token tables; [css] also has an RGBA function form. */
    val css: String
        get() {
            val rgb = (argb and 0xFFFFFF).toString(16).padStart(6, '0')
            val alpha = (argb shr 24).toString(16).padStart(2, '0')
            return "#" + rgb + if (alpha == "ff") "" else alpha
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

/** A signed density-independent position used by portable offset operations. */
@Immutable
data class DomposableOffset(
    val x: Float = 0f,
    val y: Float = 0f,
) {
    init {
        require(x.isFinite() && y.isFinite()) { "Offset must be finite" }
    }
}

/** A closed, renderer-neutral shape vocabulary for clipping and borders. */
@Immutable
sealed interface DomposableShape {
    data object Rectangle : DomposableShape

    data object Circle : DomposableShape

    data class Rounded(
        val radius: DomposableLength,
    ) : DomposableShape

    /** Logical corner radii follow layout direction, matching Compose's top-start/end model. */
    data class Corners(
        val topStart: DomposableLength = 0.dp,
        val topEnd: DomposableLength = 0.dp,
        val bottomEnd: DomposableLength = 0.dp,
        val bottomStart: DomposableLength = 0.dp,
    ) : DomposableShape
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
    val roles: Map<DomposableTypographyRole, DomposableTextAppearance> = emptyMap(),
    val emphasizedRoles: Map<DomposableTypographyRole, DomposableTextAppearance> = emptyMap(),
)

@Immutable
@Suppress("LongParameterList")
data class DomposableColors(
    val primary: DomposableColor = DomposableColor(0xFF4059AD),
    val onPrimary: DomposableColor = DomposableColor(0xFFFFFFFF),
    val surface: DomposableColor = DomposableColor(0xFFF9FAFF),
    val onSurface: DomposableColor = DomposableColor(0xFF1C2030),
    val surfaceVariant: DomposableColor = DomposableColor(0xFFECEFF8),
    val outline: DomposableColor = DomposableColor(0xFF74798C),
    val error: DomposableColor = DomposableColor(0xFFBA1A1A),
    val onError: DomposableColor? = null,
    val errorContainer: DomposableColor? = null,
    val onErrorContainer: DomposableColor? = null,
    val secondary: DomposableColor? = null,
    val onSecondary: DomposableColor? = null,
    val secondaryContainer: DomposableColor? = null,
    val onSecondaryContainer: DomposableColor? = null,
    val tertiary: DomposableColor? = null,
    val onTertiary: DomposableColor? = null,
    val tertiaryContainer: DomposableColor? = null,
    val onTertiaryContainer: DomposableColor? = null,
    val primaryContainer: DomposableColor? = null,
    val onPrimaryContainer: DomposableColor? = null,
    val background: DomposableColor? = null,
    val onBackground: DomposableColor? = null,
    val onSurfaceVariant: DomposableColor? = null,
    val outlineVariant: DomposableColor? = null,
    val inverseSurface: DomposableColor? = null,
    val inverseOnSurface: DomposableColor? = null,
    val inversePrimary: DomposableColor? = null,
    val surfaceTint: DomposableColor? = null,
    val scrim: DomposableColor? = null,
    val surfaceBright: DomposableColor? = null,
    val surfaceDim: DomposableColor? = null,
    val surfaceContainer: DomposableColor? = null,
    val surfaceContainerHigh: DomposableColor? = null,
    val surfaceContainerHighest: DomposableColor? = null,
    val surfaceContainerLow: DomposableColor? = null,
    val surfaceContainerLowest: DomposableColor? = null,
    val primaryFixed: DomposableColor? = null,
    val primaryFixedDim: DomposableColor? = null,
    val onPrimaryFixed: DomposableColor? = null,
    val onPrimaryFixedVariant: DomposableColor? = null,
    val secondaryFixed: DomposableColor? = null,
    val secondaryFixedDim: DomposableColor? = null,
    val onSecondaryFixed: DomposableColor? = null,
    val onSecondaryFixedVariant: DomposableColor? = null,
    val tertiaryFixed: DomposableColor? = null,
    val tertiaryFixedDim: DomposableColor? = null,
    val onTertiaryFixed: DomposableColor? = null,
    val onTertiaryFixedVariant: DomposableColor? = null,
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
    val components: DomposableComponentStyles = DomposableComponentStyles(),
    val designLanguage: DomposableDesignLanguage = DomposableDesignLanguage.Standard,
    val shapes: DomposableShapes = DomposableShapes(),
) {
    init {
        require(motionDurationMillis >= 0)
    }
}

enum class DomposableImageScale { Fit, Crop, Fill }

/** Typography preset; custom providers should map [Inherit] to their surrounding or host text style. */
enum class DomposableTextStyle {
    Body,
    Title,
    Label,
    Inherit,
}

enum class DomposableAlignment { Start, Center, End }

enum class DomposableLayoutAxis { Horizontal, Vertical }

enum class DomposableOrientation { Vertical, Horizontal }

enum class DomposableButtonStyle { Filled, Outlined, Text, Elevated, FilledTonal }

enum class DomposableInputPurpose { Text, Email, Password, Number, Search, Date, Time, Phone, Decimal, OneTimeCode }

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
