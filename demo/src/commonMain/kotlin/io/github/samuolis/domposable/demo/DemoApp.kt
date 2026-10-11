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
}

private object DemoCodec : DomposableRouteCodec<DemoRoute> {
    override fun encode(route: DemoRoute): String = when (route) {
        DemoRoute.Tasks -> "/tasks"
        DemoRoute.Catalog -> "/catalog"
        is DemoRoute.Detail -> "/tasks/${route.taskId}"
    }
    override fun decode(path: String): DemoRoute? = when {
        path == "/tasks" || path == "/" || path.isEmpty() -> DemoRoute.Tasks
        path == "/catalog" -> DemoRoute.Catalog
        path.startsWith("/tasks/") && path.removePrefix("/tasks/").matches(Regex("[a-z0-9-]+")) -> DemoRoute.Detail(path.removePrefix("/tasks/"))
        else -> null
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
fun DemoApp() {
    val workspace = remember { TaskWorkspaceModel() }
    val stack = rememberDomposableBackStack<DemoRoute>(DemoRoute.Tasks)
    var customButtons by rememberSaveable { mutableStateOf(false) }
    val defaultComponents = rememberDomposableMaterialComponents()
    val custom = remember(defaultComponents) { defaultComponents.copy(button = DomposableButtonRenderer { props, modifier, content -> CustomDemoButton(props, modifier, content) }) }
    val screens = remember(workspace, stack) { domposableScreenRegistry {
        register(taskListScreenDestination { TaskListScreen(workspace) { stack.add(DomposableEntryScope(DemoRoute.Detail(it))) } })
        register(taskDetailScreenDestination { TaskDetailScreen(rememberDetailModel(), workspace) { if (stack.size > 1) stack.removeLast() } })
        register(componentCatalogScreenDestination())
    } }
    DomposableTheme(components = if (customButtons) custom else defaultComponents) {
        DomposableBrowserHistory(stack, DemoCodec)
        DomposableMeasure { shellSize ->
            val shell = rememberDomposableAdaptiveInfo(shellSize)
            val selected = if (stack.last().route == DemoRoute.Catalog) 1 else 0
            val navigation: @Composable () -> Unit = {
                DomposableNavigationBar(DomposableNavigationBarProps(
                    listOf(DomposableNavigationItem("Tasks", "✓"), DomposableNavigationItem("Components", "◉")),
                    selected, { index -> stack.clear(); stack.add(DomposableEntryScope(if (index == 0) DemoRoute.Tasks else DemoRoute.Catalog)) },
                    if (shell.widthClass == DomposableWidthClass.Compact) DomposableOrientation.Horizontal else DomposableOrientation.Vertical,
                ))
            }
            DomposableScaffold(
                topBar = { DomposableRow(DomposableModifier.padding(16.dp), gap = 16.dp, alignment = DomposableAlignment.Center) {
                    DomposableText("Domposable", style = DomposableTextStyle.Title, headingLevel = 1)
                    DomposableText("One Compose API for apps and semantic HTML", style = DomposableTextStyle.Label)
                    DomposableSwitch(DomposableSwitchProps(customButtons, { customButtons = it }, "Custom buttons"))
                } },
                navigation = { if (shell.widthClass != DomposableWidthClass.Compact) navigation() },
                bottomBar = { if (shell.widthClass == DomposableWidthClass.Compact) navigation() },
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
    DomposableColumn(DomposableModifier.padding(24.dp).fillMaxWidth(), gap = 16.dp) {
        DomposableText("Task workspace", style = DomposableTextStyle.Title)
        DomposableText("Feature code owns its state. Resize to see list and detail together.")
        DomposableTextField(title, { title = it }, "New task", modifier = DomposableModifier.testId("new-task"))
        DomposableButton("Add task", { model.add(title); title = "" }, enabled = title.isNotBlank())
        model.tasks.forEach { task -> key(task.id) {
            DomposableCard(DomposableModifier.fillMaxWidth()) {
                DomposableRow(gap = 12.dp, alignment = DomposableAlignment.Center) {
                    DomposableCheckbox(task.done, { model.toggle(task.id) }, "Complete ${task.title}")
                    DomposableButton(task.title, { onOpen(task.id) }, DomposableModifier.testId("task-${task.id}"))
                }
            }
        } }
    }
}

@Composable
@DomposableScreen(route = DemoRoute.Detail::class)
fun TaskDetailScreen(model: TaskDetailModel, workspace: TaskWorkspaceModel, onBack: () -> Unit) {
    val task = workspace.tasks.firstOrNull { it.id == model.taskId }
    DomposableColumn(DomposableModifier.padding(24.dp).fillMaxWidth(), gap = 16.dp) {
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
    DomposableColumn(DomposableModifier.padding(24.dp).verticalScroll(), gap = 16.dp) {
        DomposableText("Component catalog", style = DomposableTextStyle.Title)
        DomposableText("Shared declarations, replaceable renderers, platform-specific behavior.")
        DomposableRow(gap = 12.dp) {
            DomposableButton("Filled", {})
            DomposableButton({}, style = DomposableButtonStyle.Outlined) { DomposableText("Outlined") }
            DomposableButton({}, enabled = false) { DomposableText("Disabled") }
            DomposableIconButton(DomposableIconButtonProps({}, "Favorite")) { DomposableIcon(DomposableIconProps("♡", null)) }
        }
        DomposableTextField(text, { text = it }, "Example input", modifier = DomposableModifier.testId("catalog-input"))
        DomposableTextField("", {}, "Email", purpose = DomposableInputPurpose.Email)
        DomposableCheckbox(checked, { checked = it }, "Checkbox")
        DomposableSwitch(DomposableSwitchProps(checked, { checked = it }, "Switch"))
        DomposableRadioButton(DomposableRadioButtonProps(checked, { checked = true }, "Radio option"))
        DomposableSlider(DomposableSliderProps(slider, { slider = it }, "Progress"))
        DomposableRow(gap = 12.dp) {
            DomposableChip(DomposableChipProps("Filter", { checked = !checked }, checked))
            DomposableBadge(DomposableBadgeProps("3"))
            DomposableAvatar(DomposableAvatarProps("DS", "Domposable sample"))
        }
        DomposableDivider()
        DomposableCard { DomposableColumn(gap = 8.dp) { DomposableText("Cards and surfaces"); DomposableSurface(DomposableSurfaceProps()) { DomposableText("Surface content") } } }
        DomposableBanner(DomposableBannerProps("Accessible status messages"))
        DomposableProgress(DomposableProgressProps(slider, circular = false))
        DomposableTabs(DomposableTabsProps(listOf("First", "Second"), tab, { tab = it }))
        DomposableText("Selected section: ${tab + 1}")
        DomposableRow(gap = 12.dp) {
            DomposableButton("Open dialog", { dialog = true })
            DomposableButton("Open sheet", { sheet = true })
            DomposableButton("Open menu", { menu = true })
        }
        DomposableText("Keyed collections", style = DomposableTextStyle.Title)
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
