package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavEntry
import io.github.samuolis.domposable.adaptive.DomposablePaneRecipe
import io.github.samuolis.domposable.adaptive.rememberDomposableAdaptiveInfo
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.navigation3.rememberDomposableNavEntries
import io.github.samuolis.domposable.screens.domposableScreenRegistry
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableMeasure
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableSlider
import io.github.samuolis.domposable.ui.DomposableSurface
import io.github.samuolis.domposable.ui.DomposableSwitch
import io.github.samuolis.domposable.ui.DomposableTabs
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.verticalScroll

internal val LocalRootOverlayTitleProvided = staticCompositionLocalOf { false }

@Composable
@DomposableScreen(DemoRoute.Policies::class)
internal fun NavigationPolicies() {
    val stacks = rememberSaveable(saver = RootStacks.saver) { RootStacks() }
    val models = remember { RootModels() }
    DisposableEffect(models) { onDispose { models.close() } }
    val policy = remember(stacks, models) { RootBackPolicy(stacks, models) }
    NavigationPolicyContent(stacks, models, policy)
}

@Composable
internal fun NavigationPolicyContent(
    stacks: RootStacks,
    models: RootModels,
    policy: RootBackPolicy,
) {
    val screens =
        remember(stacks, models, policy) {
            domposableScreenRegistry {
                register(RootRoute::class) {
                    if (route.overlay) {
                        DomposableSurface {
                            DomposableColumn(DomposableModifier.padding(16.dp), gap = 12.dp) {
                                if (!LocalRootOverlayTitleProvided.current) {
                                    DomposableText("Leave this entry?", headingLevel = 2)
                                }
                                DomposableText("The draft stays in its live model while this recipe is open.")
                                DomposableButton("Keep editing", policy::request)
                                DomposableButton("Leave entry", { policy.confirm(id) })
                            }
                        }
                    } else {
                        val model = models.get(route)
                        val label = "${route.root} ${route.detail.ifEmpty { "home" }}"
                        DomposableColumn(DomposableModifier.padding(16.dp), gap = 12.dp) {
                            DomposableText(label, headingLevel = 2)
                            DomposableText(
                                "Live model: ${model.instance}",
                                DomposableModifier.testId("live-${route.root}-${route.detail}"),
                            )
                            DomposableTextField(model.note, model::edit, "$label live draft")
                            if (route.detail.isEmpty()) {
                                DomposableButton("Open ${route.root} live message", { stacks.open("message") })
                            } else {
                                DomposableButton("Request back", policy::request)
                            }
                        }
                    }
                }
            }
        }
    DomposableColumn(DomposableModifier.fillMaxWidth().padding(16.dp).verticalScroll(), gap = 12.dp) {
        DomposableText("Navigation policies", headingLevel = 1)
        DomposableText(
            "Models survive root switches in an app-owned scope. Entry scopes are still disposed and recreated.",
        )
        DomposableTabs(recipeRoots, recipeRoots.indexOf(stacks.selected), {
            policy.cancel()
            stacks.select(recipeRoots[it])
        })
        DomposableSwitch(policy.confirmDirty, { policy.confirmDirty = it }, "Confirm leaving an edited entry")
        DomposableText("Back preview simulation: progress and cancel never pop an entry.")
        DomposableSlider(
            policy.preview?.progress ?: 0f,
            policy::progress,
            "Simulated back progress",
            enabled =
                stacks.active.size > 1 &&
                    !stacks.active
                        .last()
                        .route.overlay,
        )
        DomposableText("Preview progress: ${(100 * (policy.preview?.progress ?: 0f)).toInt()}%")
        DomposableButton("Cancel preview", policy::cancel, enabled = policy.preview != null)
        DomposableButton("Commit preview", policy::commit, enabled = policy.preview != null)
        DomposableMeasure(DomposableModifier.fillMaxWidth().height(480.dp)) { size ->
            val wide = DomposablePaneRecipe().isDualPane(rememberDomposableAdaptiveInfo(size))
            key(stacks.selected) {
                val entries = rememberDomposableNavEntries(stacks.active, screens)
                val routed =
                    entries.mapIndexed { index, entry ->
                        val scope = stacks.active[index]
                        NavEntry(
                            scope,
                            contentKey = entry.contentKey,
                            metadata =
                                mapOf(
                                    ROOT_ROUTE_METADATA to scope.route,
                                ) + rootPresentationMetadata(scope.route),
                        ) {
                            entry.Content()
                        }
                    }
                RootRecipeDisplay(routed, wide, policy::request)
            }
        }
    }
}
