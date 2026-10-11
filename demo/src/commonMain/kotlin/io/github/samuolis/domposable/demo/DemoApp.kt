package io.github.samuolis.domposable.demo

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import io.github.samuolis.domposable.adaptive.*
import io.github.samuolis.domposable.foundation.*
import io.github.samuolis.domposable.material.*
import io.github.samuolis.domposable.navigation3.*
import io.github.samuolis.domposable.screens.*
import io.github.samuolis.domposable.ui.*
import io.github.samuolis.domposable.demo.generated.*

sealed interface DemoRoute {
    data object Tasks : DemoRoute
    data class Detail(val taskId: String) : DemoRoute
    data object Catalog : DemoRoute
    data object Recipes : DemoRoute
    data class NotFound(val path: String) : DemoRoute
}

private object DemoCodec : DomposableRouteCodec<DemoRoute> {
    override fun encode(route: DemoRoute): String = when (route) {
        DemoRoute.Tasks -> "/tasks"
        DemoRoute.Catalog -> "/catalog"
        DemoRoute.Recipes -> "/recipes"
        is DemoRoute.NotFound -> route.path
        is DemoRoute.Detail -> "/tasks/${route.taskId}"
    }
    override fun decode(path: String): DemoRoute? = when {
        path == "/tasks" || path == "/" || path.isEmpty() -> DemoRoute.Tasks
        path == "/catalog" -> DemoRoute.Catalog
        path == "/recipes" -> DemoRoute.Recipes
        path.startsWith("/tasks/") && path.removePrefix("/tasks/").matches(Regex("[a-z0-9-]+")) -> DemoRoute.Detail(path.removePrefix("/tasks/"))
        else -> DemoRoute.NotFound(path)
    }
}

data class DemoTask(val id: String, val title: String, val done: Boolean = false)
class TaskWorkspaceModel {
    val tasks = mutableStateListOf(
        DemoTask("welcome", "Try one feature on three platforms"),
        DemoTask("adaptive", "Resize the workspace"),
        DemoTask("customize", "Replace a component renderer"),
    )
    fun toggle(id: String) { val index = tasks.indexOfFirst { it.id == id }; if (index >= 0) tasks[index] = tasks[index].copy(done = !tasks[index].done) }
    fun add(title: String) { if (title.isNotBlank()) tasks += DemoTask("task-${tasks.size + 1}", title.trim()) }
}

class TaskDetailModel(val taskId: String, note: String) { var note by mutableStateOf(note) }

/** An application-owned helper; the library never decides the model type or DI container. */
@Composable
private fun DomposableEntryScope<DemoRoute.Detail>.rememberDetailModel(): TaskDetailModel {
    val model = remember(this) { retain("demo.detail.model") {
        TaskDetailModel(route.taskId, saveableStateRegistry.consumeRestored("demo.detail.note") as? String ?: "")
    } }
    DisposableEffect(this, model) {
        val saved = saveableStateRegistry.registerProvider("demo.detail.note") { model.note }
        onDispose { saved.unregister() }
    }
    return model
}

@Composable
fun DemoApp(initialRoute: DemoRoute = DemoRoute.Tasks, browserUrls: DomposableBrowserUrls = DomposableBrowserUrls.Hash) {
    var shopping by rememberSaveable { mutableStateOf(false) }
    if (shopping) {
        ShoppingApp(browserUrls = browserUrls, onExit = { shopping = false })
        return
    }
    val workspace = remember { TaskWorkspaceModel() }
    val stack = rememberDomposableBackStack<DemoRoute>(initialRoute)
    var customButtons by rememberSaveable { mutableStateOf(false) }
    val defaultComponents = rememberDomposableMaterialComponents()
    val custom = remember(defaultComponents) { defaultComponents.copy(button = DomposableButtonRenderer { props, modifier, content -> CustomDemoButton(props, modifier, content) }) }
    val screens = remember(workspace, stack) { domposableScreenRegistry {
        register(taskListScreenDestination { TaskListScreen(workspace) { stack.add(DomposableEntryScope(DemoRoute.Detail(it))) } })
        register(taskDetailScreenDestination { TaskDetailScreen(rememberDetailModel(), workspace) { if (stack.size > 1) stack.removeLast() } })
        register(componentCatalogScreenDestination())
        register(navigationRecipesScreenDestination())
        register(unavailableScreenDestination { UnavailableScreen(route.path) { stack.clear(); stack.add(DomposableEntryScope(DemoRoute.Tasks)) } })
    } }
    DomposableTheme(tokens = DemoTokens, components = if (customButtons) custom else defaultComponents) {
        DomposableBrowserHistory(stack, DemoCodec, browserUrls)
        DomposableMeasure { shellSize ->
            val shell = rememberDomposableAdaptiveInfo(shellSize)
            val selected = when (stack.last().route) { DemoRoute.Catalog -> 1; DemoRoute.Recipes -> 2; is DemoRoute.NotFound -> -1; else -> 0 }
            fun href(path: String) = when (val mode = browserUrls) {
                DomposableBrowserUrls.Hash -> "#$path"
                is DomposableBrowserUrls.Path -> mode.basePath.trimEnd('/') + path + if (mode.trailingSlash) "/" else ""
            }
            val navigation: @Composable () -> Unit = {
                DomposableNavigationBar(DomposableNavigationBarProps(
                    listOf(DomposableNavigationItem("Tasks", "✓", href("/tasks")), DomposableNavigationItem("Components", "◉", href("/catalog")), DomposableNavigationItem("Recipes", "▦", href("/recipes"))),
                    selected, { index -> stack.clear(); stack.add(DomposableEntryScope(when (index) { 1 -> DemoRoute.Catalog; 2 -> DemoRoute.Recipes; else -> DemoRoute.Tasks })) },
                    if (shell.widthClass == DomposableWidthClass.Compact) DomposableOrientation.Horizontal else DomposableOrientation.Vertical,
                ))
            }
            DomposableScaffold(
                topBar = {
                    DomposableFlowRow(DomposableModifier.padding(16.dp).fillMaxWidth(), gap = 16.dp) {
                        DomposableText("◈ Domposable", style = DomposableTextStyle.Title, headingLevel = 1)
                        DomposableBadge(DomposableBadgeProps("PLAYGROUND"))
                        DomposableSwitch(DomposableSwitchProps(customButtons, { customButtons = it }, "Custom buttons"))
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
                val panes = if (stack.last().route is DemoRoute.Detail && DomposablePaneRecipe().isDualPane(contentInfo)) 2 else 1
                DomposableNavigation3Host(stack, screens, contentModifier.fillMaxWidth().fillMaxHeight(), panes) {
                    if (stack.size > 1) stack.removeLast()
                }
            }
        }
    }
}

@Composable
@DomposableScreen(route = DemoRoute.Tasks::class)
fun TaskListScreen(model: TaskWorkspaceModel, onOpen: (String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    DomposableColumn(DomposableModifier.padding(24.dp).fillMaxWidth().verticalScroll(), gap = 16.dp) {
        DomposableText("Task workspace", style = DomposableTextStyle.Title)
        DomposableText("One feature. Every screen size. Your architecture.")
        DomposableFlowRow(gap = 12.dp) {
            DemoMetric("${model.tasks.size}", "Tasks")
            DemoMetric("${model.tasks.count { it.done }}", "Done")
            DemoMetric("6", "Targets")
        }
        DomposableBanner(DomposableBannerProps("Open a task, write a draft, then resize. Wide layouts keep the list alongside its detail."))
        DomposableTextField(title, { title = it }, "New task", modifier = DomposableModifier.testId("new-task"))
        DomposableButton("Add task", { model.add(title); title = "" }, enabled = title.isNotBlank())
        model.tasks.forEach { task -> key(task.id) {
            DomposableCard(DomposableModifier.fillMaxWidth()) {
                DomposableColumn(DomposableModifier.padding(16.dp), gap = 8.dp) {
                    DomposableButton(task.title, { onOpen(task.id) }, DomposableModifier.testId("task-${task.id}"))
                    DomposableCheckbox(task.done, { model.toggle(task.id) }, "Completed", DomposableModifier.contentDescription("Complete ${task.title}"))
                }
            }
        } }
    }
}

@Composable
@DomposableScreen(route = DemoRoute.Detail::class)
fun TaskDetailScreen(model: TaskDetailModel, workspace: TaskWorkspaceModel, onBack: () -> Unit) {
    val task = workspace.tasks.firstOrNull { it.id == model.taskId }
    DomposableColumn(DomposableModifier.padding(24.dp).fillMaxWidth().verticalScroll(), gap = 16.dp) {
        DomposableButton("Back to tasks", onBack)
        if (task == null) DomposableBanner(DomposableBannerProps("This task is unavailable", true))
        else {
            DomposableText(task.title, style = DomposableTextStyle.Title)
            DomposableCheckbox(task.done, { workspace.toggle(task.id) }, "Completed")
            DomposableTextField(model.note, { model.note = it }, "Task notes", modifier = DomposableModifier.testId("task-notes"), multiline = true)
            DomposableText("Notes survive adaptive pane changes and browser history.")
        }
    }
}

@Composable
@DomposableScreen(route = DemoRoute.Catalog::class)
fun ComponentCatalogScreen() {
    var text by rememberSaveable { mutableStateOf("") }
    var checked by rememberSaveable { mutableStateOf(false) }
    var slider by rememberSaveable { mutableStateOf(0.5f) }
    var tab by rememberSaveable { mutableStateOf(0) }
    var dialog by remember { mutableStateOf(false) }
    var sheet by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    DomposableColumn(DomposableModifier.padding(24.dp).fillMaxWidth().verticalScroll(), gap = 16.dp) {
        DomposableText("Component catalog", style = DomposableTextStyle.Title)
        DomposableText("Explore the same controls through native Compose UI and semantic HTML.")
        DomposableFlowRow(gap = 8.dp) {
            DomposableBadge(DomposableBadgeProps("34 renderers"))
            DomposableChip(DomposableChipProps("Theme tokens", {}, true))
            DomposableLink("Renderer contracts", "https://samuolis.github.io/domposable/design-systems/")
        }
        DomposableText("Actions", style = DomposableTextStyle.Title, headingLevel = 3)
        DomposableFlowRow(gap = 12.dp) {
            DomposableButton("Filled", {})
            DomposableButton({}, style = DomposableButtonStyle.Outlined) { DomposableText("Outlined") }
            DomposableButton({}, enabled = false) { DomposableText("Disabled") }
            DomposableIconButton(DomposableIconButtonProps({}, "Favorite")) { DomposableIcon(DomposableIconProps("♡", null)) }
        }
        DomposableText("Forms and selection", style = DomposableTextStyle.Title, headingLevel = 3)
        DomposableTextField(text, { text = it }, "Example input", modifier = DomposableModifier.testId("catalog-input"))
        DomposableTextField("", {}, "Email", purpose = DomposableInputPurpose.Email)
        DomposableTextField("wrong-address", {}, "Validation example", error = "Enter an email address")
        DomposableTextField("Read only", {}, "Disabled input", enabled = false)
        DomposableTextField("sample", {}, "Password", purpose = DomposableInputPurpose.Password)
        DomposableCheckbox(checked, { checked = it }, "Checkbox")
        DomposableSwitch(DomposableSwitchProps(checked, { checked = it }, "Switch"))
        DomposableRadioButton(DomposableRadioButtonProps(checked, { checked = true }, "Radio option"))
        DomposableSlider(DomposableSliderProps(slider, { slider = it }, "Progress"))
        DomposableFlowRow(gap = 12.dp) {
            DomposableChip(DomposableChipProps("Filter", { checked = !checked }, checked))
            DomposableBadge(DomposableBadgeProps("3"))
            DomposableAvatar(DomposableAvatarProps("DS", "Domposable sample"))
        }
        DomposableImage(DomposableImageProps(DemoArtwork, "Domposable mark"), DomposableModifier.width(96.dp).height(96.dp))
        DomposableDivider()
        DomposableText("Content and feedback", style = DomposableTextStyle.Title, headingLevel = 3)
        DomposableCard { DomposableColumn(gap = 8.dp) { DomposableText("Cards and surfaces"); DomposableSurface(DomposableSurfaceProps()) { DomposableText("Surface content") } } }
        DomposableBanner(DomposableBannerProps("Accessible status messages"))
        DomposableProgress(DomposableProgressProps(slider, circular = false))
        DomposableTabs(DomposableTabsProps(listOf("First", "Second"), tab, { tab = it }))
        DomposableText("Selected section: ${tab + 1}")
        DomposableFlowRow(gap = 12.dp) {
            DomposableButton("Open dialog", { dialog = true })
            DomposableButton("Open sheet", { sheet = true })
            DomposableButton("Open menu", { menu = true })
        }
        DomposableText("Keyed collections", style = DomposableTextStyle.Title)
        DomposableText("Keyed collections", style = DomposableTextStyle.Title, headingLevel = 3)
        DomposableLazyRow(DomposableLazyRowProps((1..6).map { n -> DomposableLazyItem("tag-$n") {
            DomposableChip(DomposableChipProps("Collection $n", {}))
        } }, 8.dp), DomposableModifier.fillMaxWidth().height(64.dp))
        DomposableLazyColumn(DomposableLazyColumnProps((1..3).map { n -> DomposableLazyItem("activity-$n") {
            DomposableText("Activity $n · Stable keyed content")
        } }, 8.dp), DomposableModifier.fillMaxWidth().height(104.dp))
        DomposableLazyGrid(DomposableLazyGridProps((1..4).map { number -> DomposableLazyItem("cell-$number") { DomposableCard { DomposableText("Cell $number") } } }), DomposableModifier.height(180.dp).fillMaxWidth())
        DomposableText("Nested theme", style = DomposableTextStyle.Title)
        val base = LocalDomposableTokens.current
        DomposableTheme(base.copy(colors = base.colors.copy(primary = DomposableColor(0xFF77503B))), LocalDomposableComponents.current) {
            DomposableButton("Independent theme tokens", {})
        }
        DomposableSpacer()
        DomposableText("No ViewModel, DI or action interface is required by this screen.")
    }
    if (dialog) DomposableDialog(DomposableDialogProps("Example dialog", { dialog = false })) { DomposableText("Escape closes this dialog and restores focus.") }
    if (sheet) DomposableBottomSheet(DomposableBottomSheetProps("Example sheet", { sheet = false })) { DomposableText("Platform-adapted presentation.") }
    DomposableMenu(DomposableMenuProps(menu, { menu = false }, listOf(DomposableMenuItem("Example action", { checked = !checked }))))
}

@Composable
internal expect fun CustomDemoButton(props: DomposableButtonProps, modifier: DomposableModifier, content: @Composable () -> Unit)

@Composable
@DomposableScreen(route = DemoRoute.NotFound::class)
fun UnavailableScreen(path: String, onHome: () -> Unit) {
    DomposableColumn(DomposableModifier.padding(24.dp), gap = 16.dp) {
        DomposableText("Page not found", style = DomposableTextStyle.Title)
        DomposableText("No example matches $path")
        DomposableButton("Return to tasks", onHome)
    }
}

internal expect fun openShopDemo(onOpen: () -> Unit)
