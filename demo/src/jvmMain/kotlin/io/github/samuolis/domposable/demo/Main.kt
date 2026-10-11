package io.github.samuolis.domposable.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() =
    application {
        Window(onCloseRequest = ::exitApplication, title = "Domposable — desktop preview") { DemoApp() }
    }
