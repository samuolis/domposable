package example.koin

import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import example.koin.generated.contactsScreenEntry
import io.github.samuolis.domposable.lifecycle.DomposableViewModelEntry
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.navigation3.DomposableNavHost
import io.github.samuolis.domposable.navigation3.DomposableNavigator
import io.github.samuolis.domposable.screens.DomposableEntryLifecycle
import io.github.samuolis.domposable.screens.DomposableEntryScope
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.junit.Rule
import org.junit.Test
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinConfiguration
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class InjectionTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun generatedEntryEvaluatesInjectionDefaultsUnderItsOwner() {
        val navigator = DomposableNavigator(ContactsRoute("alex"))
        compose.setContent { ContactsApp(navigator) }
        compose.onNodeWithText("Alex").assertExists()
        val image = Image.makeFromBitmap(compose.onRoot().captureToImage().asSkiaBitmap())
        File(System.getProperty("domposable.integrationCaptures"), "koin-desktop.png").apply {
            parentFile.mkdirs()
            writeBytes(image.encodeToData(EncodedImageFormat.PNG)!!.bytes)
        }
        compose.runOnIdle { navigator.navigate(ContactsRoute("sam")) }
        compose.onNodeWithText("Sam").assertExists()
        compose.runOnIdle { navigator.goBack() }
        compose.onNodeWithText("Alex").assertExists()
    }

    @Test
    fun generatedEntryAcceptsAnExplicitModelWithoutStartingKoin() {
        val navigator = DomposableNavigator(ContactsRoute("alex"))
        val model = ContactsViewModel("alex", ContactsRepository())
        compose.setContent {
            DomposableMaterialTheme {
                DomposableNavHost(navigator, listOf(contactsScreenEntry(model)))
            }
        }
        compose.onNodeWithText("Alex").assertExists()
    }

    @Test
    fun injectionRespectsEntryStoresParametersAndDisposal() {
        val first = DomposableEntryScope(ContactsRoute("alex"))
        val second = DomposableEntryScope(ContactsRoute("sam"))
        lateinit var alex: ContactsViewModel
        lateinit var sam: ContactsViewModel
        compose.setContent {
            KoinApplication(koinConfiguration { modules(contactsModule) }) {
                DomposableViewModelEntry(first) {
                    alex = koinViewModel(parameters = { parametersOf(first.route.id) })
                    assertSame(alex, koinViewModel<ContactsViewModel>(parameters = { parametersOf(first.route.id) }))
                }
                DomposableViewModelEntry(second) {
                    sam = koinViewModel(parameters = { parametersOf(second.route.id) })
                }
            }
        }
        compose.runOnIdle {
            assertNotSame(alex, sam)
            assertSame(alex.repository, sam.repository)
            assertEquals("Alex", alex.name)
            assertEquals("Sam", sam.name)
            first.moveTo(DomposableEntryLifecycle.Destroyed)
            assertTrue(alex.cleared)
            assertFalse(sam.cleared)
            second.moveTo(DomposableEntryLifecycle.Destroyed)
            assertTrue(sam.cleared)
        }
    }
}
