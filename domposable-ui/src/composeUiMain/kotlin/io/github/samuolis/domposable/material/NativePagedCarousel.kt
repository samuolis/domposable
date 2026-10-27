package io.github.samuolis.domposable.material

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.ui.DomposableButtonProps
import io.github.samuolis.domposable.ui.DomposableCarouselControlsAppearance
import io.github.samuolis.domposable.ui.DomposableCarouselProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.toComposeModifier
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@Composable
internal fun NativePagedCarousel(
    props: DomposableCarouselProps,
    modifier: DomposableModifier,
    featureRegistry: androidx.compose.runtime.saveable.SaveableStateRegistry?,
) {
    val layout = props.pagedLayout ?: return
    if (props.items.isEmpty()) return
    val keys = props.items.map { it.key }
    val state =
        key(keys) {
            rememberPagerState(initialPage = props.currentItem.coerceIn(props.items.indices)) { props.items.size }
        }
    val currentItem by rememberUpdatedState(props.currentItem.coerceIn(props.items.indices))
    val onCurrentItemChange by rememberUpdatedState(props.onCurrentItemChange)
    val onApproachingEnd by rememberUpdatedState(props.onApproachingEnd)
    LaunchedEffect(state, currentItem) {
        if (state.currentPage != currentItem) state.animateScrollToPage(currentItem)
    }
    LaunchedEffect(state, props.items.size) {
        snapshotFlow { state.currentPage }
            .distinctUntilChanged()
            .drop(1)
            .collect { index ->
                onCurrentItemChange?.invoke(index)
                if (index >= props.items.lastIndex - 1) onApproachingEnd?.invoke(index)
            }
    }
    val scope = rememberCoroutineScope()
    BoxWithConstraints(
        modifier =
            modifier.toComposeModifier().fillMaxWidth().semantics {
                collectionInfo = CollectionInfo(1, props.items.size)
                contentDescription = props.label
            },
    ) {
        val wide = maxWidth >= layout.breakpoint.value.dp
        val inset = layout.contentInset.value.dp
        val spacing = layout.itemSpacing.value.dp
        val pageWidth =
            (
                if (wide || props.items.size == 1 || layout.compactTrailingPeek.value == 0f) {
                    maxWidth - inset * 2
                } else {
                    maxWidth - inset - spacing - layout.compactTrailingPeek.value.dp
                }
            ).coerceAtLeast(1.dp)
        val textScale =
            if (layout.heightTextScaleReference ==
                null
            ) {
                1f
            } else {
                LocalDensity.current.fontScale.coerceAtLeast(1f)
            }
        val pageHeight =
            (if (wide) layout.wideHeight else layout.compactHeight)
                ?.value
                ?.let { (it * textScale).dp }
        androidx.compose.foundation.layout.Column {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .then(pageHeight?.let { Modifier.height(it + 16.dp) } ?: Modifier),
            ) {
                HorizontalPager(
                    state = state,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .then(pageHeight?.let { Modifier.height(it + 16.dp) } ?: Modifier),
                    pageSize = PageSize.Fixed(pageWidth),
                    contentPadding = PaddingValues(horizontal = inset, vertical = 8.dp),
                    pageSpacing = spacing,
                    key = { props.items[it].key },
                ) { index ->
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .then(pageHeight?.let { Modifier.height(it) } ?: Modifier)
                                .semantics {
                                    collectionItemInfo = CollectionItemInfo(0, 1, index, 1)
                                    contentDescription = layout.labels.itemPosition(index + 1, props.items.size)
                                },
                    ) {
                        CompositionLocalProvider(LocalSaveableStateRegistry provides featureRegistry) {
                            props.items[index].content()
                        }
                    }
                }
                if (layout.showNavigation && wide && props.items.size > 1) {
                    Row(
                        modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = inset + 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        CarouselArrow(
                            label = layout.labels.previous,
                            enabled = state.currentPage > 0,
                            fallback = "‹",
                            customIcon = layout.controls.previousIcon,
                            controls = layout.controls,
                        ) { scope.launch { state.animateScrollToPage(state.currentPage - 1) } }
                        CarouselArrow(
                            label = layout.labels.next,
                            enabled = state.currentPage < props.items.lastIndex,
                            fallback = "›",
                            customIcon = layout.controls.nextIcon,
                            controls = layout.controls,
                        ) { scope.launch { state.animateScrollToPage(state.currentPage + 1) } }
                    }
                }
            }
            if (layout.showIndicators && props.items.size > 1) {
                CarouselIndicators(
                    count = props.items.size,
                    selected = state.currentPage,
                    wide = wide,
                    itemPositionLabel = layout.labels.itemPosition,
                    controls = layout.controls,
                    onSelect = { scope.launch { state.animateScrollToPage(it) } },
                )
            }
        }
    }
}

@Composable
private fun CarouselArrow(
    label: String,
    enabled: Boolean,
    fallback: String,
    customIcon: (@Composable () -> Unit)?,
    controls: DomposableCarouselControlsAppearance,
    onClick: () -> Unit,
) {
    if (!enabled) {
        Spacer(
            Modifier.size(
                (controls.navigationButton?.minWidth?.value ?: 48f).dp,
                (controls.navigationButton?.minHeight?.value ?: 48f).dp,
            ),
        )
        return
    }
    val props =
        DomposableButtonProps(
            onClick = onClick,
            enabled = enabled,
            style = DomposableButtonStyle.Outlined,
            appearance = controls.navigationButton,
        )
    NativeButton(props, DomposableModifier) {
        Box(Modifier.semantics { contentDescription = label }) {
            if (customIcon != null) customIcon() else Text(fallback)
        }
    }
}

@Composable
private fun CarouselIndicators(
    count: Int,
    selected: Int,
    wide: Boolean,
    itemPositionLabel: (Int, Int) -> String,
    controls: io.github.samuolis.domposable.ui.DomposableCarouselControlsAppearance,
    onSelect: (Int) -> Unit,
) {
    val start = (selected - 2).coerceIn(0, (count - 5).coerceAtLeast(0))
    Row(
        modifier = Modifier.fillMaxWidth().height(if (wide) 22.dp else 18.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (index in start until minOf(start + 5, count)) {
            val active = index == selected
            val color =
                (
                    if (active) {
                        controls.activeIndicator
                    } else if (wide) {
                        controls.wideInactiveIndicator
                            ?: controls.inactiveIndicator
                    } else {
                        controls.inactiveIndicator
                    }
                )?.nativeColor()
                    ?: if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            Box(
                modifier =
                    Modifier
                        .size(16.dp, 6.dp)
                        .semantics {
                            contentDescription = itemPositionLabel(index + 1, count)
                            role = Role.Button
                        }.clickable(role = Role.Button) { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.size(if (active) 16.dp else 6.dp, 6.dp).background(color, CircleShape),
                )
            }
        }
    }
}
