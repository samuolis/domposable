package io.github.samuolis.domposable.ui

import androidx.compose.runtime.Composable
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AvatarPropsTest {
    @Test
    fun contentSlotIsOptional() {
        val defaults = DomposableAvatarProps("AB", "Alex Brown")
        assertNull(defaults.content)

        val content: @Composable () -> Unit = {}
        assertNotNull(DomposableAvatarProps("AB", "Alex Brown", content = content).content)
    }
}
