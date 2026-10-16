package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableCheckboxAppearance
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableColors
import io.github.samuolis.domposable.foundation.DomposableComponentStyles
import io.github.samuolis.domposable.foundation.DomposableDesignLanguage
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableShapes
import io.github.samuolis.domposable.foundation.DomposableSwitchColors
import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.foundation.DomposableTypography
import io.github.samuolis.domposable.ui.DomposableCheckbox
import io.github.samuolis.domposable.ui.DomposableCheckboxProps
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRadioButton
import io.github.samuolis.domposable.ui.DomposableRadioButtonProps
import io.github.samuolis.domposable.ui.DomposableTabs
import io.github.samuolis.domposable.ui.DomposableTheme
import io.github.samuolis.domposable.ui.testId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeCatalogAppearanceTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun nestedThemeStylesInheritBrandValuesAndRestoreTheOuterTheme() {
        val colors = DomposableColors(primary = DomposableColor(0xFF77503B))
        val typography = DomposableTypography(bodySize = 18f, fontFamily = "Brand")
        val shapes = DomposableShapes(small = DomposableShape.Circle)
        val styles =
            DomposableComponentStyles(
                button =
                    DomposableButtonAppearance(
                        minHeight =
                            io.github.samuolis.domposable.foundation
                                .DomposableLength(48f),
                    ),
            )
        var nestedColors: DomposableColors? = null
        var nestedTypography: DomposableTypography? = null
        var nestedShapes: DomposableShapes? = null
        var nestedStyles: DomposableComponentStyles? = null
        var restoredStyles: DomposableComponentStyles? = null
        compose.setContent {
            DomposableMaterialTheme(colors = colors, typography = typography, shapes = shapes, styles = styles) {
                DomposableMaterialTheme(styles = DomposableTheme.styles.copy(button = DomposableButtonAppearance())) {
                    val currentColors = DomposableTheme.colors
                    val currentStyles = DomposableTheme.styles
                    val currentTypography = DomposableTheme.typography
                    val currentShapes = DomposableTheme.shapes
                    SideEffect {
                        nestedColors = currentColors
                        nestedStyles = currentStyles
                        nestedTypography = currentTypography
                        nestedShapes = currentShapes
                    }
                }
                val current = DomposableTheme.styles
                SideEffect { restoredStyles = current }
            }
        }
        compose.runOnIdle {
            assertEquals(colors, nestedColors)
            assertEquals(typography, nestedTypography)
            assertEquals(shapes, nestedShapes)
            assertEquals(DomposableButtonAppearance(), nestedStyles?.button)
            assertEquals(styles.card, nestedStyles?.card)
            assertEquals(styles, restoredStyles)
        }
    }

    @Test
    fun textAppearanceColorOverridesItsBaseWithoutReplacingUnspecifiedValues() {
        val base = TextStyle(color = Color.Red)
        assertEquals(Color.Red, DomposableTextAppearance().nativeTextStyleDirect(base).color)
        assertEquals(
            Color.Blue,
            DomposableTextAppearance(color = DomposableColor(0xFF0000FF)).nativeTextStyleDirect(base).color,
        )
    }

    @Test
    fun supportingTextUsesItsConfiguredColor() {
        compose.setContent {
            DomposableMaterialTheme {
                DomposableCheckbox(
                    true,
                    {},
                    "Archive",
                    description = "Kept locally",
                    appearance =
                        DomposableCheckboxAppearance(
                            descriptionAppearance = DomposableTextAppearance(color = DomposableColor(0xFF0000FF)),
                        ),
                )
            }
        }
        val layouts = mutableListOf<TextLayoutResult>()
        compose
            .onNodeWithText("Kept locally", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertEquals(
            Color.Blue,
            layouts
                .single()
                .layoutInput.style.color,
        )
    }

    @Test
    fun indeterminateCheckboxExposesMixedStateAndInvokesCallbackOnce() {
        val changes = mutableListOf<Boolean>()
        compose.setContent {
            DomposableMaterialTheme {
                DomposableCheckbox(
                    DomposableCheckboxProps(
                        checked = false,
                        onCheckedChange = changes::add,
                        label = "Archive",
                        indeterminate = true,
                    ),
                    modifier = DomposableModifier.testId("archive-choice"),
                )
            }
        }

        compose
            .onNodeWithTag("archive-choice")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.Indeterminate))
            .performClick()
        compose.runOnIdle { assertEquals(listOf(true), changes) }
    }

    @Test
    fun selectedRadioExposesItsSelectionSemantics() {
        compose.setContent {
            DomposableMaterialTheme {
                DomposableRadioButton(
                    DomposableRadioButtonProps(selected = true, onClick = {}, label = "Monthly"),
                    modifier = DomposableModifier.testId("monthly-choice"),
                )
            }
        }

        compose
            .onNodeWithTag("monthly-choice")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
    }

    @Test
    fun tabsConvenienceOverloadForwardsLeadingIconSlots() {
        compose.setContent {
            DomposableMaterialTheme {
                DomposableTabs(
                    labels = listOf("Overview", "Activity"),
                    selectedIndex = 0,
                    onSelect = {},
                    icons =
                        listOf(
                            { Box(Modifier.size(8.dp).testTag("overview-tab-icon")) },
                            { Box(Modifier.size(8.dp).testTag("activity-tab-icon")) },
                        ),
                    leadingIcons = true,
                )
            }
        }

        compose.onNodeWithTag("overview-tab-icon", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("activity-tab-icon", useUnmergedTree = true).assertExists()
    }

    @Test
    fun defaultSwitchAppearanceRetainsMaterialColors() {
        val thumb = mutableStateOf(Color.Unspecified)
        val track = mutableStateOf(Color.Unspecified)
        compose.setContent {
            DomposableMaterialTheme {
                val colors = DomposableSwitchColors().nativeSwitchColors()
                SideEffect {
                    thumb.value = colors.checkedThumbColor
                    track.value = colors.checkedTrackColor
                }
            }
        }

        compose.runOnIdle {
            assertNotEquals(Color.Unspecified, thumb.value)
            assertNotEquals(Color.Unspecified, track.value)
        }
    }

    @Test
    fun expressiveThemeUsesExpressiveMotionAndKeepsHostFontFamily() {
        val motion = mutableStateOf<MotionScheme?>(null)
        val fontFamily = mutableStateOf<FontFamily?>(null)
        val hostTypography = Typography().copy(bodyLarge = Typography().bodyLarge.copy(fontFamily = FontFamily.Cursive))
        compose.setContent {
            MaterialTheme(typography = hostTypography) {
                DomposableMaterialTheme(
                    tokens = DomposableTokens(designLanguage = DomposableDesignLanguage.Expressive),
                ) {
                    val currentMotion = MaterialTheme.motionScheme
                    val currentFontFamily = MaterialTheme.typography.bodyLarge.fontFamily
                    SideEffect {
                        motion.value = currentMotion
                        fontFamily.value = currentFontFamily
                    }
                }
            }
        }

        compose.runOnIdle {
            assertSame(MotionScheme.expressive(), motion.value)
            assertEquals(FontFamily.Cursive, fontFamily.value)
        }
    }
}
