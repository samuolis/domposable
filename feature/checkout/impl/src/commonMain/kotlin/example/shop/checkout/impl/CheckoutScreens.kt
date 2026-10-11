package example.shop.checkout.impl

import androidx.compose.runtime.*
import io.github.samuolis.domposable.foundation.*
import io.github.samuolis.domposable.ui.*
import example.shop.catalog.api.*
import example.shop.checkout.api.*
@Composable
private fun OrderSummary(model: CartState) {
    DomposableCard(DomposableModifier.fillMaxWidth()) {
        DomposableColumn(DomposableModifier.padding(16.dp), gap = 8.dp) {
            DomposableText("Order summary", style = DomposableTextStyle.Title, headingLevel = 3)
            DomposableText("Subtotal  ${shopPrice(model.subtotal)}")
            DomposableText("Delivery  ${if (model.shipping == 0) "Free" else shopPrice(model.shipping)}")
            DomposableDivider()
            DomposableText("Total  ${shopPrice(model.total)}", style = DomposableTextStyle.Title)
        }
    }
}

@Composable
@DomposableScreen(route = CheckoutRoute.Cart::class)
fun CartScreen(model: CartState, onAction: (CheckoutAction) -> Unit, artwork: ShopArtwork, onBrowse: () -> Unit, onCheckout: () -> Unit) {
    DomposableColumn(DomposableModifier.fillMaxWidth().fillMaxHeight().verticalScroll().padding(24.dp), gap = 16.dp) {
        DomposableText("Your bag", style = DomposableTextStyle.Title, headingLevel = 2)
        DomposableButton("Continue shopping", onBrowse)
        if (model.count == 0) DomposableText("Your bag is empty. Find something for your everyday.") else {
            model.products.filter { (model.quantities[it.id] ?: 0) > 0 }.forEach { product -> key(product.id) {
                DomposableCard(DomposableModifier.fillMaxWidth()) {
                    DomposableFlowRow(DomposableModifier.padding(16.dp), gap = 16.dp) {
                        artwork(product.image, product.name, DomposableModifier.width(96.dp).height(96.dp), null, DomposableImageScale.Fit)
                        DomposableColumn(gap = 8.dp) {
                            DomposableText(product.name, style = DomposableTextStyle.Title, headingLevel = 3)
                            DomposableText(shopPrice(product.cents))
                            DomposableFlowRow(gap = 8.dp) {
                                DomposableButton("−", { onAction(CheckoutAction.Quantity(product.id, -1)) }, DomposableModifier.contentDescription("Decrease ${product.name}"))
                                DomposableText("Quantity: ${model.quantities[product.id]}")
                                DomposableButton("+", { onAction(CheckoutAction.Quantity(product.id, 1)) }, DomposableModifier.contentDescription("Increase ${product.name}"), enabled = (model.quantities[product.id] ?: 0) < 20)
                            }
                        }
                    }
                }
            } }
            OrderSummary(model)
            DomposableButton("Continue to checkout", onCheckout)
        }
    }
}

@Composable
@DomposableScreen(route = CheckoutRoute.Checkout::class)
fun CheckoutScreen(model: CartState, onAction: (CheckoutAction) -> Unit, onCart: () -> Unit) {
    DomposableColumn(DomposableModifier.fillMaxWidth().fillMaxHeight().verticalScroll().padding(24.dp), gap = 16.dp) {
        DomposableText("Checkout", style = DomposableTextStyle.Title, headingLevel = 2)
        DomposableButton("Back to bag", onCart)
        DomposableBanner(DomposableBannerProps("Demo checkout. Use fictional details. No payment is collected and nothing is sent."))
        if (model.count == 0) DomposableText("Add something to your bag before checking out.") else {
            OrderSummary(model)
            DomposableTextField(model.email, { onAction(CheckoutAction.Email(it)) }, "Email address", modifier = DomposableModifier.testId("shop-email").fillMaxWidth(), purpose = DomposableInputPurpose.Email, error = if (model.showErrors && !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(model.email.trim())) "Enter a valid email" else null)
            DomposableTextField(model.name, { onAction(CheckoutAction.Name(it)) }, "Full name", modifier = DomposableModifier.fillMaxWidth(), error = if (model.showErrors && model.name.isBlank()) "Enter your name" else null)
            DomposableTextField(model.address, { onAction(CheckoutAction.Address(it)) }, "Street address", modifier = DomposableModifier.fillMaxWidth(), error = if (model.showErrors && model.address.isBlank()) "Enter an address" else null)
            DomposableTextField(model.postalCode, { onAction(CheckoutAction.PostalCode(it)) }, "Postal code", modifier = DomposableModifier.fillMaxWidth(), error = if (model.showErrors && model.postalCode.isBlank()) "Enter a postal code" else null)
            DomposableButton("Place demo order", { onAction(CheckoutAction.PlaceOrder) })
        }
    }
}

@Composable
@DomposableScreen(route = CheckoutRoute.Confirmation::class)
fun ConfirmationScreen(order: ShopOrder?, onBrowse: () -> Unit) {
    DomposableColumn(DomposableModifier.fillMaxWidth().padding(24.dp), gap = 16.dp) {
        DomposableText(if (order == null) "No order to show" else "Thanks for exploring Fieldwork.", style = DomposableTextStyle.Title, headingLevel = 2)
        if (order != null) DomposableText("Demo order: ${order.itemCount} pieces · ${shopPrice(order.totalCents)}. Nothing was purchased or sent.")
        DomposableButton("Keep exploring", onBrowse)
    }
}
