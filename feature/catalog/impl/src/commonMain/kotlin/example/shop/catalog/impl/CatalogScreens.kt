package example.shop.catalog.impl

import androidx.compose.runtime.*
import io.github.samuolis.domposable.foundation.*
import io.github.samuolis.domposable.ui.*
import example.shop.catalog.api.*
@Composable
fun StorefrontScreen(state: CatalogState, onAction: (CatalogAction) -> Unit, artwork: ShopArtwork, showHero: Boolean, onBrowse: () -> Unit, onProduct: (String) -> Unit, onArtworkLoaded: (() -> Unit)? = null) {
    DomposableMeasure(DomposableModifier.fillMaxWidth().fillMaxHeight()) { size ->
        // Wait for real constraints before starting size-dependent image requests.
        if (size.width <= 0f) return@DomposableMeasure
        val wide = size.width >= 800
        val inset = if (wide) 32 else 16
        val columns = when { size.width >= 1000 -> 3; size.width >= 600 -> 2; else -> 1 }
        val cardWidth = ((size.width - inset * 2 - 16 * (columns - 1)) / columns).coerceAtLeast(1f).dp
        DomposableColumn(DomposableModifier.fillMaxWidth().fillMaxHeight().verticalScroll().padding(inset.dp), gap = 24.dp) {
            if (showHero) {
                val copy: @Composable () -> Unit = {
                    DomposableColumn(DomposableModifier.width((if (wide) (size.width - 80) * .38f else size.width - inset * 2).coerceAtLeast(1f).dp), gap = 16.dp) {
                        DomposableText("THE EVERYDAY EDIT", style = DomposableTextStyle.Label)
                        DomposableText("Made for the way you live.", style = DomposableTextStyle.Title, headingLevel = 2)
                        DomposableText("Thoughtful objects for home, work and the places in between. Find your next everyday favourite.")
                        DomposableButton("Explore the collection", onBrowse)
                        DomposableText("Considered materials · Useful by design", style = DomposableTextStyle.Label)
                    }
                }
                val photo: @Composable () -> Unit = { artwork("shop-hero.png", "Canvas bag, linen, ceramics and warm studio light", DomposableModifier.width((if (wide) (size.width - 80) * .62f else size.width - inset * 2).coerceAtLeast(1f).dp).height((if (wide) 330 else 240).dp), onArtworkLoaded, DomposableImageScale.Crop) }
                if (wide) DomposableRow(gap = 16.dp) { copy(); photo() } else DomposableColumn(gap = 16.dp) { photo(); copy() }
            }
            DomposableText(if (showHero) "Everyday favourites" else "Shop the collection", style = DomposableTextStyle.Title, headingLevel = 2)
            DomposableFlowRow(gap = 12.dp) {
                DomposableTextField(state.search, { onAction(CatalogAction.Search(it)) }, "Search products", modifier = DomposableModifier.width(240.dp).testId("shop-search"), purpose = DomposableInputPurpose.Search)
                DomposableSwitch(DomposableSwitchProps(state.priceAscending, { onAction(CatalogAction.Sort(it)) }, "Price: low to high"))
            }
            DomposableFlowRow(gap = 8.dp) { listOf("All", "Home", "Travel").forEach { category -> DomposableChip(DomposableChipProps(category, { onAction(CatalogAction.Category(category)) }, state.category == category)) } }
            if (state.notice.isNotEmpty()) DomposableBanner(DomposableBannerProps(state.notice))
            DomposableText("${state.visibleProducts.size} thoughtfully chosen pieces", style = DomposableTextStyle.Label)
            if (state.visibleProducts.isEmpty()) {
                DomposableText("No products match your search.")
                DomposableButton("Clear filters", { onAction(CatalogAction.ClearFilters) })
            }
            DomposableFlowRow(gap = 16.dp) { state.visibleProducts.forEach { product -> key(product.id) {
                DomposableCard(DomposableModifier.width(cardWidth)) {
                    DomposableColumn(DomposableModifier.padding(16.dp).fillMaxWidth(), gap = 8.dp) {
                        artwork(product.image, product.name, DomposableModifier.fillMaxWidth().height(220.dp), null, DomposableImageScale.Fit)
                        DomposableText(product.category.uppercase(), style = DomposableTextStyle.Label)
                        DomposableText(product.name, style = DomposableTextStyle.Title, headingLevel = 3)
                        DomposableText(shopPrice(product.cents))
                        DomposableFlowRow(gap = 8.dp) {
                            DomposableButton("View details", { onProduct(product.id) }, DomposableModifier.contentDescription("View ${product.name}"))
                            DomposableButton("Add to bag", { onAction(CatalogAction.Add(product.id)) }, DomposableModifier.contentDescription("Add ${product.name} to bag"))
                        }
                    }
                }
            } } }
        }
    }
}

@Composable
@DomposableScreen(route = CatalogRoute.Product::class)
fun ProductScreen(state: ProductState, artwork: ShopArtwork, onAdd: () -> Unit, onBrowse: () -> Unit, onCart: () -> Unit) {
    val product = state.product
    DomposableColumn(DomposableModifier.fillMaxWidth().fillMaxHeight().verticalScroll().padding(24.dp), gap = 16.dp) {
        DomposableButton("Back to collection", onBrowse)
        if (product == null) DomposableText("This product is unavailable.") else {
            DomposableText(product.category.uppercase(), style = DomposableTextStyle.Label)
            DomposableText(product.name, style = DomposableTextStyle.Title, headingLevel = 2)
            artwork(product.image, product.name, DomposableModifier.fillMaxWidth().height(300.dp), null, DomposableImageScale.Fit)
            DomposableText(shopPrice(product.cents), style = DomposableTextStyle.Title)
            DomposableText(product.description)
            DomposableText("In stock · Dispatches in 2–3 working days", style = DomposableTextStyle.Label)
            DomposableFlowRow(gap = 12.dp) { DomposableButton("Add to bag", { onAdd() }); DomposableButton("View bag", onCart) }
            if (state.notice.isNotEmpty()) DomposableBanner(DomposableBannerProps(state.notice))
            DomposableDivider()
            DomposableText("The details", style = DomposableTextStyle.Title, headingLevel = 3)
            DomposableText("Designed to be used every day. Carefully chosen materials and simple, useful details. This is a fictional product in an open source example.")
        }
    }
}


@Composable @DomposableScreen(route = CatalogRoute.Home::class)
fun DiscoverScreen(state: CatalogState, onAction: (CatalogAction) -> Unit, artwork: ShopArtwork, onBrowse: () -> Unit, onProduct: (String) -> Unit, onArtworkLoaded: (() -> Unit)?) =
    StorefrontScreen(state, onAction, artwork, true, onBrowse, onProduct, onArtworkLoaded)

@Composable @DomposableScreen(route = CatalogRoute.Collection::class)
fun CollectionScreen(state: CatalogState, onAction: (CatalogAction) -> Unit, artwork: ShopArtwork, onBrowse: () -> Unit, onProduct: (String) -> Unit) =
    StorefrontScreen(state, onAction, artwork, false, onBrowse, onProduct)
