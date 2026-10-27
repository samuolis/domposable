@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package io.github.samuolis.domposable.material

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.foundation.DomposableItemAppearance
import io.github.samuolis.domposable.foundation.DomposableLength
import io.github.samuolis.domposable.ui.DomposableDropdownMenuItem
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.testId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import io.github.samuolis.domposable.foundation.dp as domposableDp
import io.github.samuolis.domposable.ui.fillMaxWidth as domposableFillMaxWidth

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeMenuItemSpacingTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun itemSpacingMatchesMaterialDefaultsAndExplicitValuesAcrossVariants() {
        val explicitSpacing = 24.domposableDp
        compose.setContent {
            DomposableMaterialTheme {
                Column(Modifier.width(360.dp)) {
                    MenuItemKind.entries.forEach { kind ->
                        MenuItemSpacingPair(kind, null)
                        MenuItemSpacingPair(kind, explicitSpacing)
                    }
                }
            }
        }

        MenuItemKind.entries.forEach { kind ->
            assertGapMatches("${kind.name}-default")
            assertGapMatches("${kind.name}-custom")
        }
    }

    private fun assertGapMatches(prefix: String) {
        val rawIcon = compose.onNodeWithTag("$prefix-raw-icon", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val rawText = compose.onNodeWithTag("$prefix-raw-text", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val sharedIcon = compose.onNodeWithTag("$prefix-shared-icon", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val sharedText = compose.onNodeWithTag("$prefix-shared-text", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertEquals(
            rawText.left.value - rawIcon.right.value,
            sharedText.left.value - sharedIcon.right.value,
            1f,
            prefix,
        )
    }
}

private enum class MenuItemKind { Regular, Checked, Selected }

@Composable
private fun MenuItemSpacingPair(
    kind: MenuItemKind,
    contentSpacing: DomposableLength?,
) {
    val prefix = "${kind.name}-${if (contentSpacing == null) "default" else "custom"}"
    val arrangement =
        contentSpacing?.let { Arrangement.spacedBy(it.value.dp) }
            ?: MenuDefaults.DropdownMenuItemHorizontalArrangement
    val sharedLeadingIcon: @Composable () -> Unit = {
        Box(Modifier.size(16.dp).testTag("$prefix-shared-icon"))
    }
    Column {
        RawMenuItem(kind, prefix, arrangement)
        DomposableDropdownMenuItem(
            textContent = { Text("Action", Modifier.testTag("$prefix-shared-text")) },
            onClick = {},
            modifier = DomposableModifier.domposableFillMaxWidth().testId("$prefix-shared"),
            checked = if (kind == MenuItemKind.Checked) false else null,
            selected = if (kind == MenuItemKind.Selected) false else null,
            leadingIcon = sharedLeadingIcon,
            appearance = DomposableItemAppearance(contentSpacing = contentSpacing),
        )
    }
}

@Composable
private fun RawMenuItem(
    kind: MenuItemKind,
    prefix: String,
    arrangement: Arrangement.Horizontal,
) {
    val leadingIcon: @Composable () -> Unit = {
        Box(Modifier.size(16.dp).testTag("$prefix-raw-icon"))
    }
    val text: @Composable () -> Unit = { Text("Action", Modifier.testTag("$prefix-raw-text")) }
    when (kind) {
        MenuItemKind.Regular -> {
            DropdownMenuItem(
                text = text,
                onClick = {},
                shape = MenuDefaults.middleItemShape,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = leadingIcon,
                horizontalArrangement = arrangement,
            )
        }

        MenuItemKind.Checked -> {
            DropdownMenuItem(
                checked = false,
                shapes = MenuDefaults.itemShapes(shape = MenuDefaults.middleItemShape),
                onCheckedChange = {},
                text = text,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = leadingIcon,
                checkedLeadingIcon = leadingIcon,
                horizontalArrangement = arrangement,
            )
        }

        MenuItemKind.Selected -> {
            DropdownMenuItem(
                selected = false,
                shapes = MenuDefaults.itemShapes(shape = MenuDefaults.middleItemShape),
                onClick = {},
                text = text,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = leadingIcon,
                selectedLeadingIcon = leadingIcon,
                horizontalArrangement = arrangement,
            )
        }
    }
}
