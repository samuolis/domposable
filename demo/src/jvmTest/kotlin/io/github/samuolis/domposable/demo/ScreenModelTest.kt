package io.github.samuolis.domposable.demo

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import example.shop.catalog.impl.ProductViewModel
import example.shop.checkout.api.CheckoutAction
import example.shop.checkout.impl.CartViewModel
import io.github.samuolis.domposable.lifecycle.*
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.screens.*
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableText
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import kotlin.test.*

class ScreenModelTest {
    @get:Rule val compose = createComposeRule()

    @Test fun metroInjectionUsesEntryStoresAndSharesOnlyTheApplicationSession() {
        val graph = createShopGraph()
        val first = DomposableEntryScope("first")
        val second = DomposableEntryScope("second")
        lateinit var firstModel: CartViewModel
        lateinit var secondModel: CartViewModel
        lateinit var product: ProductViewModel
        compose.setContent {
            CompositionLocalProvider(LocalMetroViewModelFactory provides graph.metroViewModelFactory) {
                DomposableViewModelEntry(first) {
                    firstModel = metroViewModel()
                    assertSame(firstModel, metroViewModel<CartViewModel>())
                    product = assistedMetroViewModel<ProductViewModel, ProductViewModel.Factory> { create("studio-mug") }
                }
                DomposableViewModelEntry(second) { secondModel = metroViewModel() }
            }
        }
        compose.runOnIdle {
            assertNotSame(firstModel, secondModel)
            assertSame(graph.cart, firstModel.cart)
            assertSame(firstModel.cart, secondModel.cart)
            assertEquals("studio-mug", product.state.value.product?.id)
            firstModel.dispatch(CheckoutAction.Add("studio-mug"))
            assertEquals(1, secondModel.state.value.count)
            first.moveTo(DomposableEntryLifecycle.Destroyed)
            second.moveTo(DomposableEntryLifecycle.Destroyed)
        }
    }

    @Test fun mviWrapperCollectsOnlyWhileActiveAndDispatchesToTheSuppliedPlainModel() {
        val entry = DomposableEntryScope("plain-model")
        entry.moveTo(DomposableEntryLifecycle.Resumed)
        val model = object : DomposableMvi<Int, Unit> {
            override val state = MutableStateFlow(0)
            override fun dispatch(action: Unit) { state.value++ }
        }
        compose.setContent {
            DomposableMaterialTheme {
                DomposableViewModelEntry(entry) {
                    DomposableMviScreen(model) { state, onAction ->
                        DomposableText("Value: $state")
                        DomposableButton("Increment", { onAction(Unit) })
                    }
                }
            }
        }
        compose.onNodeWithText("Increment").performClick()
        compose.onNodeWithText("Value: 1").assertExists()
        compose.runOnIdle { entry.moveTo(DomposableEntryLifecycle.Created) }
        compose.waitForIdle()
        compose.runOnIdle { model.state.value = 7 }
        compose.onNodeWithText("Value: 1").assertExists()
        compose.runOnIdle { entry.moveTo(DomposableEntryLifecycle.Resumed) }
        compose.onNodeWithText("Value: 7").assertExists()
        compose.runOnIdle { entry.moveTo(DomposableEntryLifecycle.Destroyed) }
    }
}
