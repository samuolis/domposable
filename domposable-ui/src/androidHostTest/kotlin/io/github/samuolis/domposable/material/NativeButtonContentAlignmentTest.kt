package io.github.samuolis.domposable.material

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.LayoutDirection
import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.width
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NativeButtonContentAlignmentTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun contentAlignmentRespectsPaddingAndLayoutDirection() {
        var alignment by mutableStateOf(DomposableAlignment.Start)
        var direction by mutableStateOf(LayoutDirection.Ltr)
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                DomposableMaterialTheme {
                    DomposableButton(
                        onClick = {},
                        modifier = DomposableModifier.width(240.dp).testId("control"),
                        appearance =
                            DomposableButtonAppearance(
                                contentAlignment = alignment,
                                contentPadding = DomposableInsets(start = 16.dp, end = 16.dp),
                            ),
                    ) { DomposableText("Content") }
                }
            }
        }
        for (layoutDirection in LayoutDirection.entries) {
            for (contentAlignment in DomposableAlignment.entries) {
                compose.runOnIdle {
                    direction = layoutDirection
                    alignment = contentAlignment
                }
                val bounds = compose.onNodeWithTag("control").getUnclippedBoundsInRoot()
                val copy = compose.onNodeWithText("Content", useUnmergedTree = true).getUnclippedBoundsInRoot()
                val width = copy.right.value - copy.left.value
                val expected =
                    when (contentAlignment) {
                        DomposableAlignment.Start -> {
                            if (layoutDirection ==
                                LayoutDirection.Ltr
                            ) {
                                bounds.left.value + 16
                            } else {
                                bounds.right.value - 16 - width
                            }
                        }

                        DomposableAlignment.Center -> {
                            (bounds.left.value + bounds.right.value - width) / 2
                        }

                        DomposableAlignment.End -> {
                            if (layoutDirection ==
                                LayoutDirection.Ltr
                            ) {
                                bounds.right.value - 16 - width
                            } else {
                                bounds.left.value + 16
                            }
                        }
                    }
                assertEquals(expected, copy.left.value, .5f, "$layoutDirection $contentAlignment")
            }
        }
    }
}
