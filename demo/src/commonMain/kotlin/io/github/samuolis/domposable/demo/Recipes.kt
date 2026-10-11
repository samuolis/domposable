package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.adaptive.DomposablePaneRecipe
import io.github.samuolis.domposable.adaptive.DomposableWidthClass
import io.github.samuolis.domposable.adaptive.rememberDomposableAdaptiveInfo
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableColors
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.DomposableTokens
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.navigation3.DomposableNavigation3Host
import io.github.samuolis.domposable.navigation3.rememberDomposableBackStack
import io.github.samuolis.domposable.screens.DomposableEntryScope
import io.github.samuolis.domposable.screens.domposableScreenRegistry
import io.github.samuolis.domposable.ui.DomposableBadge
import io.github.samuolis.domposable.ui.DomposableBadgeProps
import io.github.samuolis.domposable.ui.DomposableBanner
import io.github.samuolis.domposable.ui.DomposableBannerProps
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableCard
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableFlowRow
import io.github.samuolis.domposable.ui.DomposableLink
import io.github.samuolis.domposable.ui.DomposableMeasure
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableSurface
import io.github.samuolis.domposable.ui.DomposableSurfaceProps
import io.github.samuolis.domposable.ui.DomposableSwitch
import io.github.samuolis.domposable.ui.DomposableSwitchProps
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.verticalScroll
import io.github.samuolis.domposable.ui.width

internal val DemoTokens =
    DomposableTokens(
        colors =
            DomposableColors(
                primary = DomposableColor(0xFF5746B2),
                surface = DomposableColor(0xFFFCFAFF),
                surfaceVariant = DomposableColor(0xFFF0EDF8),
                onSurface = DomposableColor(0xFF242137),
            ),
        motionDurationMillis = 0,
    )

@Composable
internal fun DemoMetric(
    value: String,
    label: String,
) {
    DomposableCard(DomposableModifier.width(104.dp)) {
        DomposableColumn(DomposableModifier.padding(12.dp), gap = 4.dp) {
            DomposableText(value, style = DomposableTextStyle.Title, headingLevel = 3)
            DomposableText(label, style = DomposableTextStyle.Label)
        }
    }
}

private sealed interface RecipeRoute {
    data object List : RecipeRoute

    data object Detail : RecipeRoute

    data object Supporting : RecipeRoute
}

/** The feature only accepts application callbacks; no DI or ViewModel base type. */
@Composable
@DomposableScreen(route = DemoRoute.Recipes::class)
fun NavigationRecipesScreen() {
    val stack = rememberDomposableBackStack<RecipeRoute>(RecipeRoute.List)
    var supporting by rememberSaveable { mutableStateOf(false) }
    val screens =
        remember(stack) {
            domposableScreenRegistry {
                register(RecipeRoute.List::class) {
                    RecipePane("Inbox", "List") {
                        DomposableText("A route owns its entry and saved UI state.")
                        DomposableButton("Open message", {
                            if (stack.last().route ==
                                RecipeRoute.List
                            ) {
                                stack.add(DomposableEntryScope(RecipeRoute.Detail))
                            }
                        }, DomposableModifier.testId("recipe-open"))
                    }
                }
                register(RecipeRoute.Detail::class) {
                    var draft by rememberSaveable { mutableStateOf("") }
                    RecipePane("Message", "Detail") {
                        DomposableTextField(
                            draft,
                            { draft = it },
                            "Reply draft",
                            DomposableModifier.testId("recipe-draft"),
                        )
                        DomposableButton("Show supporting pane", {
                            if (stack.last().route !=
                                RecipeRoute.Supporting
                            ) {
                                stack.add(DomposableEntryScope(RecipeRoute.Supporting))
                            }
                        })
                        DomposableButton("Recipe back", { if (stack.size > 1) stack.removeAt(stack.lastIndex) })
                    }
                }
                register(RecipeRoute.Supporting::class) {
                    RecipePane("Context", "Supporting") {
                        DomposableText("Three entries can be visible when the feature has room.")
                        DomposableText("Changing pane count preserves the entries and draft.")
                        DomposableButton("Recipe back", { if (stack.size > 1) stack.removeAt(stack.lastIndex) })
                    }
                }
            }
        }
    DomposableColumn(DomposableModifier.padding(24.dp).fillMaxWidth().verticalScroll(), gap = 16.dp) {
        DomposableText("Navigation recipes", style = DomposableTextStyle.Title)
        DomposableText("Navigation 3 hosts the same application-owned routes on every renderer.")
        DomposableSwitch(DomposableSwitchProps(supporting, { supporting = it }, "Allow three panes"))
        DomposableButton("Reset recipe", {
            stack.clear()
            stack.add(DomposableEntryScope(RecipeRoute.List))
        })
        DomposableMeasure(DomposableModifier.fillMaxWidth().height(420.dp)) { size ->
            val info = rememberDomposableAdaptiveInfo(size)
            val count =
                when {
                    supporting && info.widthClass == DomposableWidthClass.Large -> 3
                    DomposablePaneRecipe().isDualPane(info) -> 2
                    else -> 1
                }
            DomposableNavigation3Host(stack, screens, DomposableModifier.fillMaxWidth().fillMaxHeight(), count) {
                if (stack.size > 1) stack.removeAt(stack.lastIndex)
            }
        }
        DomposableBanner(
            DomposableBannerProps("Try list/detail, add a supporting pane, type a reply, resize, and go back."),
        )
        DomposableText("Entry ownership", style = DomposableTextStyle.Title, headingLevel = 3)
        DomposableText(
            "Use entry.retain for an application-owned model and disposal. Use " +
                "rememberSaveable for UI drafts. Supply models directly, or resolve " +
                "them with Metro, Koin or your own factory.",
        )
        DomposableFlowRow(gap = 8.dp) {
            DomposableLink("Navigation guide", "https://samuolis.github.io/domposable/recipes/")
            DomposableLink("Metro and Kobweb", "https://samuolis.github.io/domposable/integrations/")
        }
    }
}

@Composable
private fun RecipePane(
    title: String,
    role: String,
    content: @Composable () -> Unit,
) {
    DomposableSurface(DomposableSurfaceProps(), DomposableModifier.fillMaxWidth().fillMaxHeight()) {
        DomposableColumn(DomposableModifier.padding(16.dp).fillMaxWidth().verticalScroll(), gap = 12.dp) {
            DomposableBadge(DomposableBadgeProps(role))
            DomposableText(title, style = DomposableTextStyle.Title, headingLevel = 3)
            content()
        }
    }
}
