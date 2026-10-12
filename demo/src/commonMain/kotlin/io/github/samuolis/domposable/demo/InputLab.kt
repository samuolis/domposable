package io.github.samuolis.domposable.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.samuolis.domposable.foundation.DomposableInputPurpose
import io.github.samuolis.domposable.foundation.DomposableScreen
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableCheckbox
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableDialog
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableSnackbar
import io.github.samuolis.domposable.ui.DomposableSwitch
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.verticalScroll

/** One shared form for native rendering, semantic HTML and Wasm interaction checks. */
@Composable
@DomposableScreen(DemoRoute.Inputs::class)
internal fun InputLab() {
    var name by rememberSaveable { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var consent by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var dialog by rememberSaveable { mutableStateOf(false) }
    var notice by rememberSaveable { mutableStateOf<String?>(null) }
    DomposableColumn(DomposableModifier.fillMaxWidth().padding(16.dp).verticalScroll(), gap = 12.dp) {
        DomposableText("Input and accessibility lab", headingLevel = 1)
        DomposableTextField(
            name,
            { name = it },
            "Display name",
            DomposableModifier.testId("input-name"),
            enabled = enabled,
            error = if (submitted && name.isBlank()) "Enter your display name" else null,
        )
        DomposableTextField(
            secret,
            { secret = it },
            "Private phrase",
            DomposableModifier.testId("input-secret"),
            purpose = DomposableInputPurpose.Password,
            enabled = enabled,
        )
        DomposableSwitch(enabled, { enabled = it }, "Editing enabled")
        DomposableCheckbox(consent, { consent = it }, "Allow local preview", enabled = enabled)
        DomposableButton("Preview", {
            submitted = true
            if (name.isNotBlank() && consent) notice = "Preview ready for $name"
        }, enabled = enabled && consent)
        notice?.let { message -> DomposableSnackbar(message, onDismiss = { notice = null }) }
        DomposableButton("Open help", { dialog = true })
    }
    if (dialog) {
        DomposableDialog("Input help", onDismiss = { dialog = false }) {
            DomposableText("Names and drafts remain in this demo on your device.")
            DomposableButton("Close help", { dialog = false })
        }
    }
}
