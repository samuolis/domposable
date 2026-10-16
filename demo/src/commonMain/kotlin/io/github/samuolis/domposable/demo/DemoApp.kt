package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.adaptive.DomposablePaneRecipe
import io.github.samuolis.domposable.adaptive.DomposableWidthClass
import io.github.samuolis.domposable.adaptive.rememberDomposableAdaptiveInfo
import io.github.samuolis.domposable.demo.generated.advancedCollectionsDestination
import io.github.samuolis.domposable.demo.generated.collectionLabDestination
import io.github.samuolis.domposable.demo.generated.componentCatalogScreenDestination
import io.github.samuolis.domposable.demo.generated.extendedComponentCatalogDestination
import io.github.samuolis.domposable.demo.generated.inputLabDestination
import io.github.samuolis.domposable.demo.generated.materialCatalogScreenDestination
import io.github.samuolis.domposable.demo.generated.navigationPoliciesDestination
import io.github.samuolis.domposable.demo.generated.navigationRecipesScreenDestination
import io.github.samuolis.domposable.demo.generated.productComponentLabDestination
import io.github.samuolis.domposable.demo.generated.rootStackRecipeDestination
import io.github.samuolis.domposable.demo.generated.taskDetailScreenDestination
import io.github.samuolis.domposable.demo.generated.taskListScreenDestination
import io.github.samuolis.domposable.demo.generated.unavailableScreenDestination
import io.github.samuolis.domposable.foundation.DomposableButtonStyle
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableInputPurpose
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableOrientation
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.DomposableStateColors
import io.github.samuolis.domposable.foundation.DomposableTextFieldAppearance
import io.github.samuolis.domposable.foundation.DomposableTextFieldColors
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.material.rememberDomposableMaterialComponents
import io.github.samuolis.domposable.navigation3.DomposableBrowserHistory
import io.github.samuolis.domposable.navigation3.DomposableBrowserUrls
import io.github.samuolis.domposable.navigation3.DomposableNavigation3Host
import io.github.samuolis.domposable.navigation3.DomposableRouteCodec
import io.github.samuolis.domposable.navigation3.rememberDomposableBackStack
import io.github.samuolis.domposable.screens.DomposableEntryScope
import io.github.samuolis.domposable.screens.domposableScreenRegistry
import io.github.samuolis.domposable.ui.DomposableAvatar
import io.github.samuolis.domposable.ui.DomposableBadge
import io.github.samuolis.domposable.ui.DomposableBanner
import io.github.samuolis.domposable.ui.DomposableBottomSheet
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableButtonProps
import io.github.samuolis.domposable.ui.DomposableButtonRenderer
import io.github.samuolis.domposable.ui.DomposableCard
import io.github.samuolis.domposable.ui.DomposableCheckbox
import io.github.samuolis.domposable.ui.DomposableChip
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableDialog
import io.github.samuolis.domposable.ui.DomposableDivider
import io.github.samuolis.domposable.ui.DomposableFlowRow
import io.github.samuolis.domposable.ui.DomposableIcon
import io.github.samuolis.domposable.ui.DomposableIconButton
import io.github.samuolis.domposable.ui.DomposableImage
import io.github.samuolis.domposable.ui.DomposableLazyColumn
import io.github.samuolis.domposable.ui.DomposableLazyGrid
import io.github.samuolis.domposable.ui.DomposableLazyRow
import io.github.samuolis.domposable.ui.DomposableLink
import io.github.samuolis.domposable.ui.DomposableMeasure
import io.github.samuolis.domposable.ui.DomposableMenu
import io.github.samuolis.domposable.ui.DomposableMenuItem
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableNavigationBar
import io.github.samuolis.domposable.ui.DomposableNavigationItem
import io.github.samuolis.domposable.ui.DomposableProgress
import io.github.samuolis.domposable.ui.DomposableRadioButton
import io.github.samuolis.domposable.ui.DomposableScaffold
import io.github.samuolis.domposable.ui.DomposableSlider
import io.github.samuolis.domposable.ui.DomposableSpacer
import io.github.samuolis.domposable.ui.DomposableSurface
import io.github.samuolis.domposable.ui.DomposableSwitch
import io.github.samuolis.domposable.ui.DomposableTabs
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.DomposableTheme
import io.github.samuolis.domposable.ui.LocalDomposableComponents
import io.github.samuolis.domposable.ui.LocalDomposableTokens
import io.github.samuolis.domposable.ui.contentDescription
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.verticalScroll
import io.github.samuolis.domposable.ui.width

sealed interface DemoRoute {
    data object Tasks : DemoRoute

    data class Detail(
        val taskId: String,
    ) : DemoRoute

    data object ExtendedCatalog : DemoRoute

    data class Material(
        val section: String = "actions",
    ) : DemoRoute

    data object Catalog : DemoRoute

    data object Recipes : DemoRoute

    data object Policies : DemoRoute

    data object Roots : DemoRoute

    data object Collections : DemoRoute

    data object AdvancedCollections : DemoRoute

    data object ProductComponents : DemoRoute

    data object Inputs : DemoRoute

    data class NotFound(
        val path: String,
    ) : DemoRoute
}

internal object DemoCodec : DomposableRouteCodec<DemoRoute> {
    override fun encode(route: DemoRoute): String =
        when (route) {
            DemoRoute.Tasks -> "/tasks"
            DemoRoute.ExtendedCatalog -> "/components"
            DemoRoute.Catalog -> "/catalog"
            is DemoRoute.Material -> "/material/${route.section}"
            DemoRoute.Recipes -> "/recipes"
            DemoRoute.Policies -> "/navigation-policies"
            DemoRoute.Roots -> "/roots"
            DemoRoute.Collections -> "/collections"
            DemoRoute.AdvancedCollections -> "/collection-recipes"
            DemoRoute.ProductComponents -> "/product-components"
            DemoRoute.Inputs -> "/inputs"
            is DemoRoute.NotFound -> route.path
            is DemoRoute.Detail -> "/tasks/${route.taskId}"
        }

    override fun decode(path: String): DemoRoute? =
        when {
            path == "/tasks" || path == "/" || path.isEmpty() -> {
                DemoRoute.Tasks
            }

            path == "/components" -> {
                DemoRoute.ExtendedCatalog
            }

            path == "/catalog" -> {
                DemoRoute.Catalog
            }

            path.startsWith("/material/") && path.removePrefix("/material/") in MaterialSections -> {
                DemoRoute.Material(path.removePrefix("/material/"))
            }

            path == "/recipes" -> {
                DemoRoute.Recipes
            }

            path == "/navigation-policies" -> {
                DemoRoute.Policies
            }

            path == "/roots" -> {
                DemoRoute.Roots
            }

            path == "/collections" -> {
                DemoRoute.Collections
            }

            path == "/collection-recipes" -> {
                DemoRoute.AdvancedCollections
            }

            path == "/product-components" -> {
                DemoRoute.ProductComponents
            }

            path == "/inputs" -> {
                DemoRoute.Inputs
            }

            path.startsWith(
                "/tasks/",
            ) &&
                path
                    .removePrefix(
                        "/tasks/",
                    ).matches(Regex("[a-z0-9-]+"))
            -> {
                DemoRoute.Detail(path.removePrefix("/tasks/"))
            }

            else -> {
                DemoRoute.NotFound(path)
            }
        }
}

data class DemoTask(
    val id: String,
    val title: String,
    val done: Boolean = false,
)

class TaskWorkspaceModel {
    val tasks =
        mutableStateListOf(
            DemoTask("welcome", "Try one feature on three platforms"),
            DemoTask("adaptive", "Resize the workspace"),
            DemoTask("customize", "Replace a component renderer"),
        )

    fun toggle(id: String) {
        val index = tasks.indexOfFirst { it.id == id }
        if (index >=
            0
        ) {
            tasks[index] = tasks[index].copy(done = !tasks[index].done)
        }
    }

    fun add(title: String) {
        if (title.isNotBlank()) tasks += DemoTask("task-${tasks.size + 1}", title.trim())
    }
}

class TaskDetailModel(
    val taskId: String,
    note: String,
) {
    var note by mutableStateOf(note)
}

/** An application-owned helper; the library never decides the model type or DI container. */
@Composable
private fun DomposableEntryScope<DemoRoute.Detail>.rememberDetailModel(): TaskDetailModel {
    val model =
        remember(this) {
            retain("demo.detail.model") {
                TaskDetailModel(
                    route.taskId,
                    saveableStateRegistry.consumeRestored("demo.detail.note") as? String ?: "",
                )
            }
        }
    DisposableEffect(this, model) {
        val saved = saveableStateRegistry.registerProvider("demo.detail.note") { model.note }
        onDispose { saved.unregister() }
    }
    return model
}

@Composable
fun DemoApp(
    initialRoute: DemoRoute = DemoRoute.Tasks,
    browserUrls: DomposableBrowserUrls = DomposableBrowserUrls.Hash,
) {
    var shopping by rememberSaveable { mutableStateOf(false) }
    if (shopping) {
        ShoppingApp(browserUrls = browserUrls, onExit = { shopping = false })
        return
    }
    val workspace = remember { TaskWorkspaceModel() }
    val stack = rememberDomposableBackStack<DemoRoute>(initialRoute)
    var customButtons by rememberSaveable { mutableStateOf(false) }
    val defaultComponents = rememberDomposableMaterialComponents()
    val custom =
        remember(defaultComponents) {
            defaultComponents.copy(
                button =
                    DomposableButtonRenderer {
                        props,
                        modifier,
                        content,
                        ->
                        CustomDemoButton(props, modifier, content)
                    },
            )
        }
    val screens =
        remember(workspace, stack) {
            domposableScreenRegistry {
                register(
                    taskListScreenDestination {
                        TaskListScreen(workspace) { stack.add(DomposableEntryScope(DemoRoute.Detail(it))) }
                    },
                )
                register(
                    taskDetailScreenDestination {
                        TaskDetailScreen(rememberDetailModel(), workspace) {
                            if (stack.size >
                                1
                            ) {
                                stack.removeAt(stack.lastIndex)
                            }
                        }
                    },
                )
                register(
                    componentCatalogScreenDestination {
                        ComponentCatalogScreen(
                            onMoreComponents = { stack.add(DomposableEntryScope(DemoRoute.ExtendedCatalog)) },
                            onMaterialCatalog = { stack.add(DomposableEntryScope(DemoRoute.Material())) },
                        )
                    },
                )
                register(
                    extendedComponentCatalogDestination {
                        ExtendedComponentCatalog(
                            onCollections = { stack.add(DomposableEntryScope(DemoRoute.Collections)) },
                            onInputs = { stack.add(DomposableEntryScope(DemoRoute.Inputs)) },
                            onProductComponents = { stack.add(DomposableEntryScope(DemoRoute.ProductComponents)) },
                        )
                    },
                )
                register(
                    navigationRecipesScreenDestination {
                        NavigationRecipesScreen(
                            onRoots = { stack.add(DomposableEntryScope(DemoRoute.Roots)) },
                            onPolicies = { stack.add(DomposableEntryScope(DemoRoute.Policies)) },
                        )
                    },
                )
                register(navigationPoliciesDestination())
                register(rootStackRecipeDestination())

                register(
                    collectionLabDestination {
                        CollectionLab { stack.add(DomposableEntryScope(DemoRoute.AdvancedCollections)) }
                    },
                )
                register(advancedCollectionsDestination())
                register(inputLabDestination())
                register(
                    materialCatalogScreenDestination {
                        MaterialCatalogScreen(route.section) { section ->
                            stack[stack.lastIndex] = DomposableEntryScope(DemoRoute.Material(section))
                        }
                    },
                )
                register(productComponentLabDestination())
                register(
                    unavailableScreenDestination {
                        UnavailableScreen(route.path) {
                            stack.clear()
                            stack.add(DomposableEntryScope(DemoRoute.Tasks))
                        }
                    },
                )
            }
        }
    DomposableTheme(tokens = DemoTokens, components = if (customButtons) custom else defaultComponents) {
        DomposableBrowserHistory(stack, DemoCodec, browserUrls)
        DomposableMeasure { shellSize ->
            val shell = rememberDomposableAdaptiveInfo(shellSize)
            val selected =
                when (stack.last().route) {
                    DemoRoute.Catalog,
                    DemoRoute.ExtendedCatalog,
                    DemoRoute.Collections,
                    DemoRoute.AdvancedCollections,
                    DemoRoute.Inputs,
                    DemoRoute.ProductComponents,
                    -> 1

                    DemoRoute.Recipes, DemoRoute.Roots, DemoRoute.Policies -> 2

                    is DemoRoute.NotFound -> -1

                    else -> 0
                }

            fun href(path: String) =
                when (val mode = browserUrls) {
                    DomposableBrowserUrls.Hash -> {
                        "#$path"
                    }

                    is DomposableBrowserUrls.Path -> {
                        mode.basePath.trimEnd('/') + path +
                            if (mode.trailingSlash) "/" else ""
                    }
                }
            val navigation: @Composable () -> Unit = {
                DomposableNavigationBar(
                    items =
                        listOf(
                            DomposableNavigationItem("Tasks", "✓", href("/tasks")),
                            DomposableNavigationItem("Components", "◉", href("/catalog")),
                            DomposableNavigationItem("Recipes", "▦", href("/recipes")),
                        ),
                    selectedIndex = selected,
                    onSelect = { index ->
                        stack.clear()
                        stack.add(
                            DomposableEntryScope(
                                when (index) {
                                    1 -> DemoRoute.Catalog
                                    2 -> DemoRoute.Recipes
                                    else -> DemoRoute.Tasks
                                },
                            ),
                        )
                    },
                    orientation =
                        if (shell.widthClass ==
                            DomposableWidthClass.Compact
                        ) {
                            DomposableOrientation.Horizontal
                        } else {
                            DomposableOrientation.Vertical
                        },
                )
            }
            DomposableScaffold(
                topBar = {
                    DomposableFlowRow(DomposableModifier.padding(16.dp).fillMaxWidth(), gap = 16.dp) {
                        DomposableText("◈ Domposable", style = DomposableTextStyle.Title, headingLevel = 1)
                        DomposableBadge(text = "PLAYGROUND")
                        DomposableSwitch(
                            checked = customButtons,
                            onCheckedChange = { customButtons = it },
                            label = "Custom buttons",
                        )
                        DomposableButton("Shop demo", { openShopDemo { shopping = true } })
                    }
                },
                navigation = { if (shell.widthClass != DomposableWidthClass.Compact) navigation() },
                bottomBar = {
                    DomposableColumn(DomposableModifier.fillMaxWidth()) {
                        if (shell.widthClass == DomposableWidthClass.Compact) navigation()
                        DomposableFlowRow(DomposableModifier.padding(8.dp), gap = 8.dp) {
                            DomposableLink("Guides", "https://samuolis.github.io/domposable/")
                            DomposableLink("Compare web", "https://samuolis.github.io/domposable/compare/")
                            DomposableLink("Source", "https://github.com/samuolis/domposable")
                        }
                    }
                },
            ) {
                val contentInfo = rememberDomposableAdaptiveInfo(availableContentSize)
                val panes =
                    if (stack.last().route is DemoRoute.Detail &&
                        DomposablePaneRecipe().isDualPane(contentInfo)
                    ) {
                        2
                    } else {
                        1
                    }
                DomposableNavigation3Host(stack, screens, contentModifier.fillMaxWidth().fillMaxHeight(), panes) {
                    if (stack.size > 1) stack.removeAt(stack.lastIndex)
                }
            }
        }
    }
}

@Composable
@DomposableScreen(route = DemoRoute.Tasks::class)
fun TaskListScreen(
    model: TaskWorkspaceModel,
    onOpen: (String) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    DomposableColumn(DomposableModifier.padding(24.dp).fillMaxWidth().verticalScroll(), gap = 16.dp) {
        DomposableText("Task workspace", style = DomposableTextStyle.Title)
        DomposableText("One feature. Every screen size. Your architecture.")
        DomposableFlowRow(gap = 12.dp) {
            DemoMetric("${model.tasks.size}", "Tasks")
            DemoMetric("${model.tasks.count { it.done }}", "Done")
            DemoMetric("6", "Targets")
        }
        DomposableBanner(
            message = "Open a task, write a draft, then resize. Wide layouts keep the list alongside its detail.",
        )
        DomposableTextField(title, { title = it }, "New task", modifier = DomposableModifier.testId("new-task"))
        DomposableButton("Add task", {
            model.add(title)
            title = ""
        }, enabled = title.isNotBlank())
        model.tasks.forEach { task ->
            key(task.id) {
                DomposableCard(DomposableModifier.fillMaxWidth()) {
                    DomposableColumn(DomposableModifier.padding(16.dp), gap = 8.dp) {
                        DomposableButton(task.title, { onOpen(task.id) }, DomposableModifier.testId("task-${task.id}"))
                        DomposableCheckbox(task.done, {
                            model.toggle(task.id)
                        }, "Completed", DomposableModifier.contentDescription("Complete ${task.title}"))
                    }
                }
            }
        }
    }
}

@Composable
@DomposableScreen(route = DemoRoute.Detail::class)
fun TaskDetailScreen(
    model: TaskDetailModel,
    workspace: TaskWorkspaceModel,
    onBack: () -> Unit,
) {
    val task = workspace.tasks.firstOrNull { it.id == model.taskId }
    DomposableColumn(DomposableModifier.padding(24.dp).fillMaxWidth().verticalScroll(), gap = 16.dp) {
        DomposableButton("Back to tasks", onBack)
        if (task == null) {
            DomposableBanner(message = "This task is unavailable", isError = true)
        } else {
            DomposableText(task.title, style = DomposableTextStyle.Title)
            DomposableCheckbox(task.done, { workspace.toggle(task.id) }, "Completed")
            DomposableTextField(model.note, {
                model.note = it
            }, "Task notes", modifier = DomposableModifier.testId("task-notes"), multiline = true)
            DomposableText("Notes survive adaptive pane changes and browser history.")
        }
    }
}

@Composable
@DomposableScreen(route = DemoRoute.Catalog::class)
fun ComponentCatalogScreen(
    onMoreComponents: () -> Unit = {},
    onMaterialCatalog: () -> Unit = {},
) {
    var text by rememberSaveable { mutableStateOf("") }
    var checked by rememberSaveable { mutableStateOf(false) }
    var slider by rememberSaveable { mutableFloatStateOf(0.5f) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var dialog by remember { mutableStateOf(false) }
    var sheet by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    var themedLoading by remember { mutableStateOf(false) }
    var themedNotes by rememberSaveable { mutableStateOf("") }
    var themedPhone by rememberSaveable { mutableStateOf("") }
    DomposableColumn(DomposableModifier.padding(24.dp).fillMaxWidth().verticalScroll(), gap = 16.dp) {
        DomposableText("Component catalog", style = DomposableTextStyle.Title)
        DomposableText("Explore the same controls through native Compose UI and semantic HTML.")
        DomposableFlowRow(gap = 8.dp) {
            DomposableBadge(text = "Shared components")
            DomposableChip(label = "Theme tokens", onClick = {}, selected = true)
            DomposableLink("Renderer contracts", "https://samuolis.github.io/domposable/design-systems/")
        }
        DomposableText("Actions", style = DomposableTextStyle.Title, headingLevel = 3)
        DomposableFlowRow(gap = 12.dp) {
            DomposableButton("More components", onMoreComponents)
            DomposableButton("Filled", {})
            DomposableButton({}, style = DomposableButtonStyle.Outlined) { DomposableText("Outlined") }
            DomposableButton({}, enabled = false) { DomposableText("Disabled") }
            DomposableIconButton(onClick = {}, label = "Favorite") { DomposableIcon(symbol = "♡", description = null) }
        }
        DomposableText("Forms and selection", style = DomposableTextStyle.Title, headingLevel = 3)
        DomposableTextField(text, { text = it }, "Example input", modifier = DomposableModifier.testId("catalog-input"))
        DomposableTextField("", {}, "Email", purpose = DomposableInputPurpose.Email)
        DomposableTextField("wrong-address", {}, "Validation example", error = "Enter an email address")
        DomposableTextField("Read only", {}, "Disabled input", enabled = false)
        DomposableTextField("sample", {}, "Password", purpose = DomposableInputPurpose.Password)
        DomposableCheckbox(checked, { checked = it }, "Checkbox")
        DomposableSwitch(checked = checked, onCheckedChange = { checked = it }, label = "Switch")
        DomposableRadioButton(selected = checked, onClick = { checked = true }, label = "Radio option")
        DomposableSlider(value = slider, onValueChange = { slider = it }, label = "Progress")
        DomposableFlowRow(gap = 12.dp) {
            DomposableChip(label = "Filter", onClick = { checked = !checked }, selected = checked)
            DomposableBadge(text = "3")
            DomposableAvatar(initials = "DS", description = "Domposable sample")
        }
        DomposableImage(
            source = DEMO_ARTWORK,
            description = "Domposable mark",
            modifier = DomposableModifier.width(96.dp).height(96.dp),
        )
        DomposableDivider()
        DomposableText("Content and feedback", style = DomposableTextStyle.Title, headingLevel = 3)
        DomposableCard {
            DomposableColumn(gap = 8.dp) {
                DomposableText("Cards and surfaces")
                DomposableSurface { DomposableText("Surface content") }
            }
        }
        DomposableBanner(message = "Accessible status messages")
        DomposableProgress(value = slider, circular = false)
        DomposableTabs(labels = listOf("First", "Second"), selectedIndex = tab, onSelect = { tab = it })
        DomposableText("Selected section: ${tab + 1}")
        DomposableFlowRow(gap = 12.dp) {
            DomposableButton("Open dialog", { dialog = true })
            DomposableButton("Open sheet", { sheet = true })
            DomposableButton("Open menu", { menu = true })
        }
        DomposableText("Keyed collections", style = DomposableTextStyle.Title, headingLevel = 3)
        DomposableLazyRow(
            items = (1..6).toList(),
            key = { "tag-$it" },
            modifier = DomposableModifier.fillMaxWidth().height(64.dp),
            gap = 8.dp,
        ) { number -> DomposableChip("Collection $number", {}) }
        DomposableLazyColumn(
            items = (1..3).toList(),
            key = { "activity-$it" },
            modifier = DomposableModifier.fillMaxWidth().height(104.dp),
            gap = 8.dp,
        ) { number -> DomposableText("Activity $number · Stable keyed content") }
        DomposableLazyGrid(
            items = (1..4).toList(),
            key = { "cell-$it" },
            modifier = DomposableModifier.height(180.dp).fillMaxWidth(),
        ) { number -> DomposableCard { DomposableText("Cell $number") } }
        DomposableLink(
            "Open the full Material and expressive catalog",
            "#/material/actions",
            onClick = onMaterialCatalog,
        )
        DomposableText("Nested theme", style = DomposableTextStyle.Title)
        val base = LocalDomposableTokens.current
        DomposableTheme(
            base.copy(
                colors = base.colors.copy(primary = DomposableColor(0xFF77503B)),
                button =
                    base.button.copy(
                        container = DomposableColor(0xFF77503B),
                        content = DomposableColor(0xFFFFFFFF),
                        shape = DomposableShape.Rounded(6.dp),
                        minHeight = 48.dp,
                    ),
                textField =
                    DomposableTextFieldAppearance(
                        colors =
                            DomposableTextFieldColors(
                                outline =
                                    DomposableStateColors(
                                        unfocused = DomposableColor(0xFF74798C),
                                        focused = DomposableColor(0xFF77503B),
                                    ),
                            ),
                        floatingLabel = true,
                        autoSize = true,
                    ),
            ),
            LocalDomposableComponents.current,
        ) {
            DomposableButton("Independent theme tokens", {})
            DomposableButton({}, enabled = false) { DomposableText("Themed disabled") }
            DomposableButton(
                DomposableButtonProps(
                    onClick = { themedLoading = true },
                    loading = themedLoading,
                    loadingLabel = "Saving themed action",
                ),
                modifier = DomposableModifier.testId("themed-action"),
            ) { DomposableText("Themed action") }
            DomposableButton("Reset themed action", { themedLoading = false })
            DomposableTextField(
                io.github.samuolis.domposable.ui.DomposableTextFieldProps(
                    value = themedPhone,
                    onValueChange = { themedPhone = it },
                    label = "Contact phone",
                    purpose = DomposableInputPurpose.Phone,
                    autocomplete = io.github.samuolis.domposable.foundation.DomposableAutocomplete.Phone,
                    imeAction = io.github.samuolis.domposable.foundation.DomposableImeAction.Done,
                ),
            )
            DomposableTextField(
                io.github.samuolis.domposable.ui.DomposableTextFieldProps(
                    value = themedNotes,
                    onValueChange = { themedNotes = it },
                    label = "Theme notes",
                    multiline = true,
                    minLines = 2,
                    maxLines = 4,
                ),
                modifier = DomposableModifier.testId("themed-notes"),
            )
        }
        DomposableSpacer()
        DomposableText("Open More components for app bars, selection fields and feedback controls.")
    }
    if (dialog) {
        DomposableDialog(title = "Example dialog", onDismiss = {
            dialog = false
        }) { DomposableText("Escape closes this dialog and restores focus.") }
    }
    if (sheet) {
        DomposableBottomSheet(title = "Example sheet", onDismiss = {
            sheet = false
        }) { DomposableText("Platform-adapted presentation.") }
    }
    DomposableMenu(
        expanded = menu,
        onDismiss = { menu = false },
        items =
            listOf(
                DomposableMenuItem("Example action", {
                    checked =
                        !checked
                }),
            ),
    )
}

@Composable
internal expect fun CustomDemoButton(
    props: DomposableButtonProps,
    modifier: DomposableModifier,
    content: @Composable () -> Unit,
)

@Composable
@DomposableScreen(route = DemoRoute.NotFound::class)
fun UnavailableScreen(
    path: String,
    onHome: () -> Unit,
) {
    DomposableColumn(DomposableModifier.padding(24.dp), gap = 16.dp) {
        DomposableText("Page not found", style = DomposableTextStyle.Title)
        DomposableText("No example matches $path")
        DomposableButton("Return to tasks", onHome)
    }
}

internal expect fun openShopDemo(onOpen: () -> Unit)
