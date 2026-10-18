package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableComponent
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableProgressAppearance
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.DomposableTextFieldAppearance
import io.github.samuolis.domposable.foundation.dp

@Immutable
data class DomposableCarouselLabels(
    val previous: String,
    val next: String,
    val itemPosition: (position: Int, total: Int) -> String,
) {
    init {
        require(previous.isNotBlank() && next.isNotBlank())
    }
}

@Immutable
data class DomposableCarouselControlsAppearance(
    val navigationButton: DomposableButtonAppearance? = null,
    val activeIndicator: DomposableColor? = null,
    val inactiveIndicator: DomposableColor? = null,
    val previousIcon: (@Composable () -> Unit)? = null,
    val nextIcon: (@Composable () -> Unit)? = null,
    val wideInactiveIndicator: DomposableColor? = null,
)

/** Layout and accessibility options for a one-item-at-a-time responsive carousel. */
@Immutable
data class DomposablePagedCarouselLayout(
    val compactHeight: DomposableLength? = null,
    val wideHeight: DomposableLength? = null,
    /** When set, card heights grow with BodyMedium text size relative to this reference size. */
    val heightTextScaleReference: DomposableLength? = null,
    val breakpoint: DomposableLength = 600.dp,
    val contentInset: DomposableLength = 16.dp,
    val itemSpacing: DomposableLength = 12.dp,
    val compactTrailingPeek: DomposableLength = 14.dp,
    val showIndicators: Boolean = true,
    val showNavigation: Boolean = true,
    val labels: DomposableCarouselLabels,
    val controls: DomposableCarouselControlsAppearance = DomposableCarouselControlsAppearance(),
) {
    init {
        require(breakpoint.value > 0f)
        require(contentInset.value >= 0f && itemSpacing.value >= 0f && compactTrailingPeek.value >= 0f)
        require(compactHeight == null || compactHeight.value > 0f)
        require(wideHeight == null || wideHeight.value > 0f)
        require(heightTextScaleReference == null || heightTextScaleReference.value > 0f)
    }
}

@DomposableComponent("Carousel")
data class DomposableCarouselProps(
    val items: List<DomposableLazyItem>,
    val label: String,
    val variant: DomposableCarouselVariant = DomposableCarouselVariant.MultiBrowse,
    val preferredItemWidth: DomposableLength = 240.dp,
    val minSmallItemWidth: DomposableLength = 40.dp,
    val maxSmallItemWidth: DomposableLength = 56.dp,
    val spacing: DomposableLength = 8.dp,
    val currentItem: Int = 0,
    val onCurrentItemChange: ((Int) -> Unit)? = null,
    val pagedLayout: DomposablePagedCarouselLayout? = null,
    val onApproachingEnd: ((Int) -> Unit)? = null,
) {
    init {
        require(items.map { it.key }.distinct().size == items.size)
        require(
            preferredItemWidth.value > 0f && minSmallItemWidth.value > 0f &&
                maxSmallItemWidth.value >= minSmallItemWidth.value,
        )
    }
}

@Composable
@Suppress("LongParameterList")
fun DomposableCarousel(
    items: List<DomposableLazyItem>,
    label: String,
    variant: DomposableCarouselVariant = DomposableCarouselVariant.MultiBrowse,
    preferredItemWidth: DomposableLength = 240.dp,
    minSmallItemWidth: DomposableLength = 40.dp,
    maxSmallItemWidth: DomposableLength = 56.dp,
    spacing: DomposableLength = 8.dp,
    modifier: DomposableModifier = DomposableModifier,
    currentItem: Int = 0,
    onCurrentItemChange: ((Int) -> Unit)? = null,
    pagedLayout: DomposablePagedCarouselLayout? = null,
    onApproachingEnd: ((Int) -> Unit)? = null,
) = DomposableCarousel(
    DomposableCarouselProps(
        items,
        label,
        variant,
        preferredItemWidth,
        minSmallItemWidth,
        maxSmallItemWidth,
        spacing,
        currentItem = currentItem,
        onCurrentItemChange = onCurrentItemChange,
        pagedLayout = pagedLayout,
        onApproachingEnd = onApproachingEnd,
    ),
    modifier,
)

@DomposableComponent("PullToRefresh")
data class DomposablePullToRefreshProps(
    val refreshing: Boolean,
    val onRefresh: () -> Unit,
    val enabled: Boolean = true,
    val label: String = "Refresh",
    val threshold: DomposableLength = 80.dp,
    val appearance: DomposableProgressAppearance? = null,
) {
    init {
        require(threshold.value > 0f)
    }
}

@Composable
@Suppress("LongParameterList")
fun DomposablePullToRefresh(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    enabled: Boolean = true,
    label: String = "Refresh",
    threshold: DomposableLength = 80.dp,
    appearance: DomposableProgressAppearance? = null,
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable () -> Unit,
) = DomposablePullToRefresh(
    DomposablePullToRefreshProps(refreshing, onRefresh, enabled, label, threshold, appearance),
    modifier,
    content,
)

@DomposableComponent("SwipeToDismiss")
data class DomposableSwipeToDismissProps(
    val onDismiss: (DomposableSwipeDirection) -> Unit,
    val enabled: Boolean = true,
    val allowStartToEnd: Boolean = true,
    val allowEndToStart: Boolean = true,
    val startActionLabel: String = "Dismiss",
    val endActionLabel: String = "Dismiss",
    val background: @Composable (DomposableSwipeDirection?) -> Unit = {},
) {
    init {
        require(allowStartToEnd || allowEndToStart)
    }
}

@Composable
@Suppress("LongParameterList")
fun DomposableSwipeToDismiss(
    onDismiss: (DomposableSwipeDirection) -> Unit,
    enabled: Boolean = true,
    allowStartToEnd: Boolean = true,
    allowEndToStart: Boolean = true,
    startActionLabel: String = "Dismiss",
    endActionLabel: String = "Dismiss",
    background: @Composable (DomposableSwipeDirection?) -> Unit = {},
    modifier: DomposableModifier = DomposableModifier,
    content: @Composable () -> Unit,
) = DomposableSwipeToDismiss(
    DomposableSwipeToDismissProps(
        onDismiss,
        enabled,
        allowStartToEnd,
        allowEndToStart,
        startActionLabel,
        endActionLabel,
        background,
    ),
    modifier,
    content,
)
