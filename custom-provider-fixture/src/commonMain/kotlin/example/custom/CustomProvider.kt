package example.custom
import io.github.samuolis.domposable.ui.DomposableComponents

// Compile-only provider fixture: every contract is implementable without Material.
// A compile fixture deliberately implements every renderer contract in one declaration.
@Suppress("LongMethod")
fun independentProvider(onRender: (String) -> Unit): DomposableComponents =
    DomposableComponents(
        form = { _, _, content ->
            onRender("Form")
            content()
        },
        disclosure = { props, _, content ->
            onRender("Disclosure")
            if (props.expanded) content()
        },
        richHtml = { props, _, _ -> onRender("RichHtml: ${props.rawHtml}") },
        lineChart = { props, _, _ -> props.series.forEach { onRender("LineChart: ${it.key}") } },
        valueChart = { props, _, _ -> props.series.forEach { onRender("ValueChart: ${it.key}") } },
        dataTable = {
            props,
            _,
            _,
            ->
            props.rows.forEach { row -> props.columns.forEach { col -> props.cellContent(row, col) } }
        },
        theme = { _, _, content ->
            onRender("Theme")
            content()
        },
        column = { _, _, content ->
            onRender("Column")
            content()
        },
        row = { _, _, content ->
            onRender("Row")
            content()
        },
        flowRow = { _, _, content ->
            onRender("FlowRow")
            content()
        },
        link = { _, _, content ->
            onRender("Link")
            content()
        },
        box = { _, _, content ->
            onRender("Box")
            content()
        },
        lazyColumn = { props, _, content ->
            onRender("LazyColumn")
            props.window?.let { onRender("Window: ${it.rowHeight.value}/${it.viewportHeight.value}/${it.overscan}") }
            content()
        },
        lazyRow = { _, _, content ->
            onRender("LazyRow")
            content()
        },
        lazyGrid = { _, _, content ->
            onRender("LazyGrid")
            content()
        },
        table = { props, _, _ ->
            onRender("Table")
            props.rows.forEach { row -> row.cells.forEach { cell -> cell() } }
        },
        text = { _, _, content ->
            onRender("Text")
            content()
        },
        button = { _, _, content ->
            onRender("Button")
            content()
        },
        iconButton = { _, _, content ->
            onRender("IconButton")
            content()
        },
        textField = { _, _, content ->
            onRender("TextField")
            content()
        },
        checkbox = { _, _, content ->
            onRender("Checkbox")
            content()
        },
        switch = { _, _, content ->
            onRender("Switch")
            content()
        },
        radioButton = { _, _, content ->
            onRender("RadioButton")
            content()
        },
        slider = { _, _, content ->
            onRender("Slider")
            content()
        },
        card = { _, _, content ->
            onRender("Card")
            content()
        },
        surface = { _, _, content ->
            onRender("Surface")
            content()
        },
        image = { _, _, content ->
            onRender("Image")
            content()
        },
        icon = { _, _, content ->
            onRender("Icon")
            content()
        },
        divider = { _, _, content ->
            onRender("Divider")
            content()
        },
        spacer = { _, _, content ->
            onRender("Spacer")
            content()
        },
        badge = { _, _, content ->
            onRender("Badge")
            content()
        },
        chip = { _, _, content ->
            onRender("Chip")
            content()
        },
        avatar = { _, _, content ->
            onRender("Avatar")
            content()
        },
        banner = { _, _, content ->
            onRender("Banner")
            content()
        },
        progress = { _, _, content ->
            onRender("Progress")
            content()
        },
        tabs = { _, _, content ->
            onRender("Tabs")
            content()
        },
        navigationBar = { _, _, content ->
            onRender("NavigationBar")
            content()
        },
        menu = { _, _, content ->
            onRender("Menu")
            content()
        },
        dialog = { _, _, content ->
            onRender("Dialog")
            content()
        },
        bottomSheet = { _, _, content ->
            onRender("BottomSheet")
            content()
        },
        topAppBar = { _, _, content ->
            onRender("TopAppBar")
            content()
        },
        floatingActionButton = { _, _, content ->
            onRender("FloatingActionButton")
            content()
        },
        snackbar = { _, _, content ->
            onRender("Snackbar")
            content()
        },
        select = { _, _, content ->
            onRender("Select")
            content()
        },
        segmentedButtons = { _, _, content ->
            onRender("SegmentedButtons")
            content()
        },
        rangeSlider = { _, _, content ->
            onRender("RangeSlider")
            content()
        },
        tooltip = { _, _, content ->
            onRender("Tooltip")
            content()
        },
        appBar = { _, _, content ->
            onRender("AppBar")
            content()
        },
        autocomplete = { _, _, content ->
            onRender("Autocomplete")
            content()
        },
        badgedBox = { _, _, content ->
            onRender("BadgedBox")
            content()
        },
        bottomAppBar = { _, _, content ->
            onRender("BottomAppBar")
            content()
        },
        bottomSheetScaffold = { _, _, content ->
            onRender("BottomSheetScaffold")
            content()
        },
        buttonGroup = { _, _, content ->
            onRender("ButtonGroup")
            content()
        },
        carousel = { _, _, content ->
            onRender("Carousel")
            content()
        },
        datePicker = { _, _, content ->
            onRender("DatePicker")
            content()
        },
        dateRangePicker = { _, _, content ->
            onRender("DateRangePicker")
            content()
        },
        dragHandle = { _, _, content ->
            onRender("DragHandle")
            content()
        },
        dropdownMenuItem = { _, _, content ->
            onRender("DropdownMenuItem")
            content()
        },
        floatingActionButtonMenu = { _, _, content ->
            onRender("FloatingActionButtonMenu")
            content()
        },
        floatingToolbar = { _, _, content ->
            onRender("FloatingToolbar")
            content()
        },
        label = { _, _, content ->
            onRender("Label")
            content()
        },
        listItem = { _, _, content ->
            onRender("ListItem")
            content()
        },
        loadingIndicator = { _, _, content ->
            onRender("LoadingIndicator")
            content()
        },
        menuGroup = { _, _, content ->
            onRender("MenuGroup")
            content()
        },
        multiChoiceSegmentedButtons = { _, _, content ->
            onRender("MultiChoiceSegmentedButtons")
            content()
        },
        navigationDrawer = { _, _, content ->
            onRender("NavigationDrawer")
            content()
        },
        navigationDrawerItem = { _, _, content ->
            onRender("NavigationDrawerItem")
            content()
        },
        pullToRefresh = { _, _, content ->
            onRender("PullToRefresh")
            content()
        },
        scrim = { _, _, content ->
            onRender("Scrim")
            content()
        },
        scrollField = { _, _, content ->
            onRender("ScrollField")
            content()
        },
        searchBar = { _, _, content ->
            onRender("SearchBar")
            content()
        },
        snackbarHost = { _, _, content ->
            onRender("SnackbarHost")
            content()
        },
        splitButton = { _, _, content ->
            onRender("SplitButton")
            content()
        },
        swipeToDismiss = { _, _, content ->
            onRender("SwipeToDismiss")
            content()
        },
        timePicker = { _, _, content ->
            onRender("TimePicker")
            content()
        },
        toggleButton = { _, _, content ->
            onRender("ToggleButton")
            content()
        },
        wideNavigationRail = { _, _, content ->
            onRender("WideNavigationRail")
            content()
        },
        wideNavigationRailItem = { _, _, content ->
            onRender("WideNavigationRailItem")
            content()
        },
        scaffold = { _, _, content ->
            onRender("Scaffold")
            content()
        },
    )
