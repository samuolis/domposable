package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableCardVariant
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableProgressAppearance
import io.github.samuolis.domposable.foundation.DomposableProgressVariant
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.DomposableTabsVariant
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTopAppBarVariant
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableAvatar
import io.github.samuolis.domposable.ui.DomposableBadge
import io.github.samuolis.domposable.ui.DomposableBadgedBox
import io.github.samuolis.domposable.ui.DomposableBanner
import io.github.samuolis.domposable.ui.DomposableBottomAppBar
import io.github.samuolis.domposable.ui.DomposableBottomAppBarVariant
import io.github.samuolis.domposable.ui.DomposableBottomSheet
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableCard
import io.github.samuolis.domposable.ui.DomposableCarousel
import io.github.samuolis.domposable.ui.DomposableCarouselVariant
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableDialog
import io.github.samuolis.domposable.ui.DomposableDisclosure
import io.github.samuolis.domposable.ui.DomposableDivider
import io.github.samuolis.domposable.ui.DomposableDragHandle
import io.github.samuolis.domposable.ui.DomposableFlowRow
import io.github.samuolis.domposable.ui.DomposableIcon
import io.github.samuolis.domposable.ui.DomposableIconButton
import io.github.samuolis.domposable.ui.DomposableLazyItem
import io.github.samuolis.domposable.ui.DomposableListItem
import io.github.samuolis.domposable.ui.DomposableListItemPosition
import io.github.samuolis.domposable.ui.DomposableListItemVariant
import io.github.samuolis.domposable.ui.DomposableLoadingIndicator
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableNavigationBar
import io.github.samuolis.domposable.ui.DomposableNavigationDrawer
import io.github.samuolis.domposable.ui.DomposableNavigationDrawerItem
import io.github.samuolis.domposable.ui.DomposableNavigationItem
import io.github.samuolis.domposable.ui.DomposableProgress
import io.github.samuolis.domposable.ui.DomposablePullToRefresh
import io.github.samuolis.domposable.ui.DomposableRichHtml
import io.github.samuolis.domposable.ui.DomposableSnackbar
import io.github.samuolis.domposable.ui.DomposableSnackbarHost
import io.github.samuolis.domposable.ui.DomposableSurface
import io.github.samuolis.domposable.ui.DomposableSwipeToDismiss
import io.github.samuolis.domposable.ui.DomposableSwitch
import io.github.samuolis.domposable.ui.DomposableTabs
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTooltip
import io.github.samuolis.domposable.ui.DomposableTopAppBar
import io.github.samuolis.domposable.ui.DomposableVectorIcon
import io.github.samuolis.domposable.ui.DomposableWideNavigationRail
import io.github.samuolis.domposable.ui.DomposableWideNavigationRailItem
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.rememberDomposableSnackbarHostState
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.width
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun MaterialSurfaces() {
    var dialog by rememberSaveable { mutableStateOf(false) }
    var disclosure by rememberSaveable { mutableStateOf(false) }
    var sheet by rememberSaveable { mutableStateOf(false) }
    DomposableCardVariant.entries.forEach { variant ->
        DomposableCard(variant = variant, modifier = DomposableModifier.fillMaxWidth()) {
            DomposableColumn(DomposableModifier.padding(16.dp), gap = 8.dp) {
                DomposableText("${variant.name} card")
                DomposableText("Container, text, shape, border and elevation come from the theme.")
            }
        }
    }
    DomposableSurface(appearance = DomposableSurfaceAppearance(container = DomposableColor(0xFFF3E8DE))) {
        DomposableText("Local surface override")
    }
    DomposableListItem(
        "Standard list item",
        supportingText = "Leading and trailing content are caller-owned",
        leadingContent = {
            DomposableIcon(
                vector =
                    DomposableVectorIcon(
                        pathData =
                            "M12 2A10 10 0 1 0 12 22A10 10 0 1 0 12 2" +
                                "M12 4A8 8 0 1 1 12 20A8 8 0 1 1 12 4" +
                                "M12 8A4 4 0 1 0 12 16A4 4 0 1 0 12 8",
                        evenOdd = true,
                    ),
                description = null,
                modifier = DomposableModifier.size(16.dp),
            )
        },
        trailingContent = { DomposableText("›") },
    )
    listOf(
        DomposableListItemPosition.First,
        DomposableListItemPosition.Middle,
        DomposableListItemPosition.Last,
    ).forEach { position ->
        DomposableListItem(
            "Segmented ${position.name}",
            supportingText = "Expressive list structure",
            variant = DomposableListItemVariant.Segmented,
            position = position,
        )
    }
    DomposableDisclosure("Expandable details", disclosure, {
        disclosure = !disclosure
    }) { DomposableText("Disclosure content") }
    DomposableRichHtml(
        """<p>Safe <strong>rich text</strong> with a <a href="https://example.com">link</a>.</p>""",
    )
    DomposableButton("Open dialog", { dialog = true })
    DomposableButton("Open modal sheet", { sheet = true })
    if (dialog) {
        DomposableDialog("Document options", { dialog = false }, actions = {
            DomposableButton("Done", {
                dialog =
                    false
            })
        }) { DomposableText("A shared modal with platform focus and dismissal behavior.") }
    }
    if (sheet) {
        DomposableBottomSheet("Sheet options", { sheet = false }) { DomposableText("A shared sheet") }
    }
}

@Composable
internal fun MaterialNavigation() {
    var selected by rememberSaveable { mutableStateOf(0) }
    var drawer by rememberSaveable { mutableStateOf(false) }
    var wide by rememberSaveable { mutableStateOf(false) }
    val items =
        listOf(
            DomposableNavigationItem("Home", "⌂"),
            DomposableNavigationItem("Saved", "★"),
            DomposableNavigationItem("Account", "◉"),
        )
    DomposableTabs(listOf("Home", "Saved", "Account"), selected, { selected = it })
    DomposableTabs(listOf("Overview", "Activity", "Documents"), selected, {
        selected = it
    }, variant = DomposableTabsVariant.Secondary, scrollable = true)
    DomposableNavigationBar(items, selected, { selected = it }, label = "Bottom navigation example")
    DomposableNavigationBar(
        items,
        selected,
        { selected = it },
        orientation = DomposableOrientation.Vertical,
        modifier = DomposableModifier.height(280.dp),
        label = "Rail navigation example",
    )
    DomposableButton("Open navigation drawer", { drawer = true })
    DomposableNavigationDrawer(drawer, { drawer = false }) {
        items.forEachIndexed { index, item ->
            DomposableNavigationDrawerItem(item.label, selected == index, {
                selected =
                    index
                ; drawer = false
            }, icon = { DomposableText(item.symbol) })
        }
    }
    DomposableButton("Expand wide rail", { wide = !wide })
    DomposableWideNavigationRail(wide, modifier = DomposableModifier.height(320.dp)) {
        items.forEachIndexed { index, item ->
            DomposableWideNavigationRailItem(item.label, selected == index, {
                selected =
                    index
            }, railExpanded = wide, icon = { DomposableText(item.symbol) })
        }
    }
    DomposableTopAppBarVariant.entries.forEach { variant ->
        DomposableTopAppBar("${variant.name} app bar", variant = variant)
    }
    DomposableTopAppBar(
        "Flexible heading with subtitle",
        subtitle = "Navigation and actions keep their own slots",
        variant = DomposableTopAppBarVariant.MediumFlexible,
        navigationIcon = { DomposableIconButton({ selected = 0 }, "Go back") { DomposableText("‹") } },
        actions = { DomposableIconButton({ selected = 2 }, "More actions") { DomposableText("⋮") } },
    )
    DomposableTopAppBar(
        "Large flexible heading",
        subtitle = "Supporting text",
        variant = DomposableTopAppBarVariant.LargeFlexible,
    )
    DomposableTopAppBar(
        "Custom title typography",
        subtitle = "Per-call styles keep the shared API",
        variant = DomposableTopAppBarVariant.TwoRows,
        appearance = DomposableSurfaceAppearance(textAppearance = DomposableTextAppearance(fontSize = 31f)),
    )
    DomposableBottomAppBar(
        variant = DomposableBottomAppBarVariant.Flexible,
    ) { DomposableText("Flexible bottom actions") }
}

@Composable
internal fun MaterialFeedback() {
    var animate by rememberSaveable { mutableStateOf(false) }
    var showTooltip by rememberSaveable { mutableStateOf(false) }
    DomposableSwitch(animate, { animate = it }, "Animate loading indicators")
    val messages = rememberDomposableSnackbarHostState()
    val scope = rememberCoroutineScope()
    var messageResult by remember { mutableStateOf("No queued message") }
    DomposableButton("Queue message", {
        scope.launch {
            messageResult = messages.showSnackbar("Document saved", "Undo", withDismissAction = true).name
        }
    })
    DomposableText(messageResult, modifier = DomposableModifier.testId("material-snackbar-result"))
    DomposableSnackbarHost(messages)
    DomposableBadgedBox(badge = { DomposableBadge("3") }) {
        DomposableButton("Notifications", {})
    }
    DomposableFlowRow(gap = 16.dp) {
        DomposableBadge("3")
        DomposableAvatar("AB", "Profile avatar")
        DomposableLoadingIndicator("Expressive loading", progress = if (animate) null else 0.6f)
        DomposableLoadingIndicator("Contained loading", contained = true, progress = if (animate) null else 0.6f)
        DomposableLoadingIndicator("Loading progress", progress = 0.6f)
    }
    DomposableProgressVariant.entries.forEach { variant ->
        DomposableProgress(
            value = 0.6f,
            label = "Download ${variant.name}",
            variant = variant,
            appearance = DomposableProgressAppearance(waveSpeed = if (animate) null else 0.dp),
        )
        DomposableProgress(
            value = 0.6f,
            label = "Linear download ${variant.name}",
            appearance = DomposableProgressAppearance(waveSpeed = if (animate) null else 0.dp),
            circular = false,
            variant = variant,
            modifier = DomposableModifier.fillMaxWidth(),
        )
    }
    DomposableSnackbar("Changes saved", actionLabel = "Undo", onAction = {})
    DomposableTooltip("Plain tooltip") { DomposableButton("Tooltip target", {}) }
    DomposableTooltip("Helpful details", title = "Rich tooltip", action = {
        DomposableButton("Learn more", { showTooltip = false })
    }, visible = showTooltip, onDismiss = { showTooltip = false }) {
        DomposableButton("Rich tooltip target", { showTooltip = true })
    }
    DomposableBanner("Informational message")
    DomposableDivider()
}

@Composable
internal fun MaterialGestures() {
    var refreshing by rememberSaveable { mutableStateOf(false) }
    var dismissed by rememberSaveable { mutableStateOf(false) }
    var resize by rememberSaveable { mutableStateOf(200f) }
    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(500)
            refreshing = false
        }
    }
    val items =
        (1..6).map { index ->
            DomposableLazyItem("item-$index") {
                DomposableCard { DomposableText("Carousel $index", modifier = DomposableModifier.padding(16.dp)) }
            }
        }
    DomposableCarouselVariant.entries.forEach { variant ->
        DomposableText(variant.name)
        DomposableCarousel(
            items,
            "${variant.name} carousel",
            variant = variant,
            modifier = DomposableModifier.fillMaxWidth().height(160.dp),
        )
    }
    DomposablePullToRefresh(refreshing, {
        refreshing = true
    }, modifier = DomposableModifier.fillMaxWidth().height(160.dp)) {
        DomposableButton("Refresh content", { refreshing = true })
    }
    if (!dismissed) {
        DomposableSwipeToDismiss(
            { dismissed = true },
            startActionLabel = "Archive document",
            endActionLabel = "Delete document",
            background = { DomposableText("Dismiss document") },
            modifier = DomposableModifier.testId("material-swipe-document"),
        ) {
            DomposableListItem("Swipe or use the dismissal action", supportingText = "Keyboard and touch both work")
        }
    }
    DomposableButton("Restore dismissed item", { dismissed = false })
    DomposableDragHandle("Resize panel", { resize = (resize + it).coerceIn(120f, 320f) })
    DomposableSurface(modifier = DomposableModifier.width(resize.toInt().dp).height(80.dp)) {
        DomposableText("Panel width: ${resize.toInt()}", modifier = DomposableModifier.padding(8.dp))
    }
}
