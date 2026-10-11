package io.github.samuolis.domposable.demo

import io.github.samuolis.domposable.navigation3.DomposableNavigator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24])
class NavigationCompatibilityTest {
    @Test
    fun backNavigationWorksOnTheMinimumSupportedAndroidVersion() {
        val navigator = DomposableNavigator("home")
        navigator.navigate("product")
        assertTrue(navigator.goBack())
        assertEquals("home", navigator.currentRoute)
        assertFalse(navigator.goBack())
    }
}
