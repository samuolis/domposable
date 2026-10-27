package io.github.samuolis.domposable.material

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import io.github.samuolis.domposable.ui.DomposableDialogProps
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalComposeUiApi::class)
class NativeDialogScrimTest {
    @Test
    fun composeWindowPropertiesUseTheExplicitScrimOpacity() {
        val props = dialogProperties(DomposableDialogProps("Saving", {}, scrimOpacity = 0.5f))
        assertEquals(Color.Black.copy(alpha = 0.5f), props.scrimColor)
    }
}
