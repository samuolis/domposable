package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableCheckboxAppearance
import io.github.samuolis.domposable.foundation.DomposableChipVariant
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableComponentStyles
import io.github.samuolis.domposable.foundation.DomposableControlPlacement
import io.github.samuolis.domposable.foundation.DomposableDesignLanguage
import io.github.samuolis.domposable.foundation.DomposableFabSize
import io.github.samuolis.domposable.foundation.DomposableFontWeight
import io.github.samuolis.domposable.foundation.DomposableIconButtonVariant
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableItemColors
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableProgressAppearance
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.foundation.DomposableSelectionColors
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableSurfaceAppearance
import io.github.samuolis.domposable.foundation.DomposableSwitchAppearance
import io.github.samuolis.domposable.foundation.DomposableSwitchColors
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.ui.DomposableActionItem
import io.github.samuolis.domposable.ui.DomposableAppBar
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableButtonGroup
import io.github.samuolis.domposable.ui.DomposableCheckbox
import io.github.samuolis.domposable.ui.DomposableChip
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableDropdownMenuItem
import io.github.samuolis.domposable.ui.DomposableFloatingActionButton
import io.github.samuolis.domposable.ui.DomposableFloatingActionButtonMenu
import io.github.samuolis.domposable.ui.DomposableFloatingToolbar
import io.github.samuolis.domposable.ui.DomposableFlowRow
import io.github.samuolis.domposable.ui.DomposableIconToggleButton
import io.github.samuolis.domposable.ui.DomposableLabel
import io.github.samuolis.domposable.ui.DomposableMenu
import io.github.samuolis.domposable.ui.DomposableMenuGroup
import io.github.samuolis.domposable.ui.DomposableMenuItem
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableMultiChoiceSegmentedButtons
import io.github.samuolis.domposable.ui.DomposableRadioButton
import io.github.samuolis.domposable.ui.DomposableRangeSlider
import io.github.samuolis.domposable.ui.DomposableSegmentedButtons
import io.github.samuolis.domposable.ui.DomposableSlider
import io.github.samuolis.domposable.ui.DomposableSplitButton
import io.github.samuolis.domposable.ui.DomposableSwitch
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableToggleButton
import io.github.samuolis.domposable.ui.DomposableToggleState
import io.github.samuolis.domposable.ui.DomposableTriStateCheckbox
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.verticalScroll
import io.github.samuolis.domposable.ui.width

internal val MaterialSections =
    listOf("actions", "selection", "inputs", "surfaces", "navigation", "feedback", "gestures", "scaffold")

@Composable
@DomposableScreen(route = DemoRoute.Material::class)
fun MaterialCatalogScreen(
    section: String = "actions",
    onSection: (String) -> Unit = {},
) {
    var expressive by rememberSaveable { mutableStateOf(true) }
    var customized by rememberSaveable { mutableStateOf(false) }
    val tokens = materialCatalogTokens(expressive, customized)
    DomposableMaterialTheme(tokens) {
        DomposableColumn(
            DomposableModifier
                .fillMaxWidth()
                .verticalScroll()
                .padding(16.dp)
                .testId("material-catalog-scroll"),
            gap = 16.dp,
        ) {
            DomposableText("Material catalog", style = DomposableTextStyle.Title, headingLevel = 1)
            DomposableText("The same feature code renders Material Compose UI or semantic HTML.")
            DomposableSwitch(expressive, {
                expressive = it
            }, "Expressive theme", modifier = DomposableModifier.testId("material-expressive"))
            DomposableSwitch(customized, {
                customized = it
            }, "Component overrides", modifier = DomposableModifier.testId("material-overrides"))
            DomposableFlowRow(gap = 8.dp) {
                MaterialSections.forEach { name ->
                    DomposableButton(
                        name.replaceFirstChar {
                            it.uppercase()
                        },
                        { onSection(name) },
                        modifier = DomposableModifier.testId("material-section-$name"),
                    )
                }
            }
            DomposableText(
                section.replaceFirstChar { it.uppercase() },
                style = DomposableTextStyle.Title,
                headingLevel = 2,
                modifier = DomposableModifier.testId("material-section-heading"),
            )
            when (section) {
                "actions" -> MaterialActions()
                "selection" -> MaterialSelection()
                "inputs" -> MaterialInputs()
                "surfaces" -> MaterialSurfaces()
                "navigation" -> MaterialNavigation()
                "feedback" -> MaterialFeedback()
                "gestures" -> MaterialGestures()
                "scaffold" -> MaterialScaffold()
                else -> DomposableText("Choose a component family.")
            }
        }
    }
}

private val WarmContainer = DomposableColor(0xFFF3E8DE)
private val OnWarmContainer = DomposableColor(0xFF402D21)
private val WarmAccent = DomposableColor(0xFF946749)

internal fun materialCatalogTokens(
    expressive: Boolean,
    customized: Boolean,
): DomposableTokens =
    DemoTokens.copy(
        designLanguage = if (expressive) DomposableDesignLanguage.Expressive else DomposableDesignLanguage.Standard,
        components =
            if (customized) {
                DomposableComponentStyles(
                    card =
                        DomposableSurfaceAppearance(
                            container = WarmContainer,
                            content = OnWarmContainer,
                            border = WarmAccent,
                            shape = DomposableShape.Rounded(20.dp),
                        ),
                    checkbox =
                        DomposableCheckboxAppearance(
                            colors = DomposableSelectionColors(selected = WarmAccent),
                        ),
                    switch =
                        DomposableSwitchAppearance(
                            colors = DomposableSwitchColors(checkedTrack = WarmAccent),
                        ),
                    chip =
                        DomposableItemAppearance(
                            colors =
                                DomposableItemColors(
                                    selectedContainer = WarmContainer,
                                    selectedContent = OnWarmContainer,
                                ),
                        ),
                    progress = DomposableProgressAppearance(color = WarmAccent),
                )
            } else {
                DomposableComponentStyles()
            },
    )

@Composable
internal fun MaterialActions() {
    var notice by remember { mutableStateOf("Choose an action") }
    var checked by rememberSaveable { mutableStateOf(false) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val actions =
        listOf(
            DomposableActionItem("save", "Save", { notice = "Saved" }),
            DomposableActionItem("share", "Share", { notice = "Shared" }),
            DomposableActionItem("archive", "Archive", { notice = "Archived" }),
        )
    DomposableText(notice, modifier = DomposableModifier.testId("material-action-notice"))
    DomposableFlowRow(gap = 8.dp) {
        DomposableButtonStyle.entries.forEach { style ->
            DomposableButton(
                onClick = { notice = style.name },
                style = style,
                appearance = null,
            ) { DomposableText(style.name) }
        }
    }
    DomposableFlowRow(gap = 8.dp) {
        DomposableButtonStyle.entries.forEach { style ->
            DomposableButton(onClick = {}, style = style, enabled = false) {
                DomposableText("Disabled ${style.name}")
            }
        }
    }
    DomposableFlowRow(gap = 8.dp) {
        DomposableIconButtonVariant.entries.forEach { variant ->
            DomposableIconToggleButton(checked, {
                checked = it
            }, "Favorite ${variant.name}", variant = variant) { DomposableText("★") }
        }
    }
    DomposableButtonStyle.entries.filter { it != DomposableButtonStyle.Text }.forEach { style ->
        DomposableToggleButton(checked, { checked = it }, style = style) { DomposableText("${style.name} toggle") }
    }
    DomposableButtonGroup(actions, "Document actions")
    DomposableSplitButton("Save document", {
        notice = "Document saved"
    }, "Save options", { expanded = !expanded }, expanded = expanded)
    DomposableMenu(
        expanded,
        { expanded = false },
        actions.map { DomposableMenuItem(it.label, it.onClick, it.enabled) },
    ) {
        DomposableMenuGroup {
            DomposableDropdownMenuItem("Pin document", { checked = !checked }, checked = checked)
            DomposableDropdownMenuItem("Current folder", { notice = "Folder selected" }, selected = true)
        }
    }
    DomposableFlowRow(gap = 12.dp) {
        DomposableFabSize.entries.forEach { size ->
            DomposableFloatingActionButton({
                notice = "Create ${size.name}"
            }, "Create ${size.name}", size = size) { DomposableText("+") }
        }
    }
    DomposableFloatingActionButton({ notice = "Compose" }, "Compose", extended = true) { DomposableText("+") }
    DomposableFloatingToolbar(expanded = true) { DomposableButton("Undo", { notice = "Undo" }) }
    DomposableAppBar(actions, "Overflow actions", modifier = DomposableModifier.width(240.dp))
    DomposableFloatingActionButtonMenu(expanded, { expanded = it }, "More document actions", actions)
}

@Composable
internal fun MaterialSelection() {
    var checked by rememberSaveable { mutableStateOf(false) }
    var triState by rememberSaveable(stateSaver = enumSaver<DomposableToggleState>()) {
        mutableStateOf(DomposableToggleState.Indeterminate)
    }
    var selected by rememberSaveable { mutableStateOf(0) }
    var multiple by remember { mutableStateOf(setOf(0, 2)) }
    var value by rememberSaveable { mutableStateOf(0.4f) }
    var range by remember { mutableStateOf(0.2f..0.8f) }
    DomposableCheckbox(checked, { checked = it }, "Receive updates", description = "Optional product news")
    DomposableCheckbox(checked, {
        checked = it
    }, "Trailing control", controlPlacement = DomposableControlPlacement.Trailing)
    DomposableTriStateCheckbox(triState, {
        triState =
            when (triState) {
                DomposableToggleState.Off -> DomposableToggleState.On
                DomposableToggleState.On -> DomposableToggleState.Indeterminate
                DomposableToggleState.Indeterminate -> DomposableToggleState.Off
            }
    }, "Select all documents")
    DomposableSwitch(checked, { checked = it }, "Enable notifications")
    DomposableRadioButton(selected == 0, { selected = 0 }, "Personal")
    DomposableRadioButton(selected == 1, { selected = 1 }, "Business")
    DomposableFlowRow(gap = 8.dp) {
        DomposableChipVariant.entries.forEach { variant ->
            DomposableChip(variant.name, {
                checked = !checked
            }, selected = checked, variant = variant, leadingIcon = { DomposableText("+") })
        }
    }
    DomposableChip(
        "Configured filter",
        { checked = !checked },
        selected = checked,
        appearance =
            DomposableItemAppearance(
                selectedBorderWidth = 3.dp,
                selectedTextAppearance = DomposableTextAppearance(fontWeight = DomposableFontWeight.SemiBold),
            ),
    )
    DomposableChip("Elevated filter", { checked = !checked }, selected = checked, elevated = true)
    DomposableSegmentedButtons(listOf("Day", "Week", "Month"), selected, { selected = it }, "Period")
    DomposableMultiChoiceSegmentedButtons(listOf("Email", "Push", "SMS"), multiple, { multiple = it }, "Channels")
    DomposableLabel(label = { DomposableText("Volume ${(value * 100).toInt()}") }) {
        DomposableSlider(value, { value = it }, "Volume")
    }
    DomposableSlider(value, {
        value = it
    }, "Vertical volume", orientation = DomposableOrientation.Vertical, modifier = DomposableModifier.height(180.dp))
    DomposableRangeSlider(range, { range = it }, "Minimum price", "Maximum price")
}
