package io.github.samuolis.domposable.demo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.samuolis.domposable.ui.*

@Composable
internal actual fun CustomDemoButton(props: DomposableButtonProps, modifier: DomposableModifier, content: @Composable () -> Unit) {
    Box(modifier.toComposeModifier().border(BorderStroke(2.dp, Color(LocalDomposableTokens.current.colors.primary.argb))).clickable(enabled = props.enabled, role = Role.Button, onClick = props.onClick).padding(12.dp)) { content() }
}
