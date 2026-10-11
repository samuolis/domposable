package example.shop.catalog.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import example.shop.catalog.api.CatalogAction
import example.shop.catalog.api.CatalogState
import example.shop.catalog.api.ProductState
import example.shop.catalog.api.ShopArtwork
import example.shop.catalog.api.shopPrice
import io.github.samuolis.domposable.foundation.DomposableImageScale
import io.github.samuolis.domposable.foundation.DomposableInputPurpose
import io.github.samuolis.domposable.foundation.DomposableTextStyle
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableBanner
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableCard
import io.github.samuolis.domposable.ui.DomposableChip
import io.github.samuolis.domposable.ui.DomposableColumn
import io.github.samuolis.domposable.ui.DomposableDivider
import io.github.samuolis.domposable.ui.DomposableFlowRow
import io.github.samuolis.domposable.ui.DomposableMeasure
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableRow
import io.github.samuolis.domposable.ui.DomposableSwitch
import io.github.samuolis.domposable.ui.DomposableText
import io.github.samuolis.domposable.ui.DomposableTextField
import io.github.samuolis.domposable.ui.contentDescription
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxWidth
import io.github.samuolis.domposable.ui.height
import io.github.samuolis.domposable.ui.padding
import io.github.samuolis.domposable.ui.size
import io.github.samuolis.domposable.ui.testId
import io.github.samuolis.domposable.ui.verticalScroll
import io.github.samuolis.domposable.ui.width

@Composable
internal fun StorefrontContent(
    state: CatalogState,
    onAction: (CatalogAction) -> Unit,
    artwork: ShopArtwork,
    showHero: Boolean,
    onBrowse: () -> Unit,
    onProduct: (String) -> Unit,
    onArtworkLoaded: (() -> Unit)? = null,
) {
    DomposableMeasure(DomposableModifier.fillMaxWidth().fillMaxHeight()) { size ->
        // Wait for real constraints before starting size-dependent image requests.
        if (size.width <= 0f) return@DomposableMeasure
        val wide = size.width >= 800
        val inset = if (wide) 32 else 16
        val columns =
            when {
                size.width >= 1000 -> 3
                size.width >= 600 -> 2
                else -> 1
            }
        val cardWidth = ((size.width - inset * 2 - 16 * (columns - 1)) / columns).coerceAtLeast(1f).dp
        DomposableColumn(
            DomposableModifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll()
                .padding(inset.dp),
            gap = 24.dp,
        ) {
            if (showHero) {
                val copy: @Composable () -> Unit = {
                    DomposableColumn(
                        DomposableModifier.width(
                            (
                                if (wide) {
                                    (size.width - 80) * .38f
                                } else {
                                    size.width -
                                        inset * 2
                                }
                            ).coerceAtLeast(1f).dp,
                        ),
                        gap = 16.dp,
                    ) {
                        DomposableText("THE EVERYDAY EDIT", style = DomposableTextStyle.Label)
                        DomposableText(
                            "Made for the way you live.",
                            style = DomposableTextStyle.Title,
                            headingLevel = 2,
                        )
                        DomposableText(
                            "Thoughtful objects for home, work and the places in between. Find your " +
                                "next everyday favourite.",
                        )
                        DomposableButton("Explore the collection", onBrowse)
                        DomposableText("Considered materials · Useful by design", style = DomposableTextStyle.Label)
                    }
                }
                val photo: @Composable () -> Unit = {
                    artwork(
                        "shop-hero.png",
                        "Canvas bag, linen, ceramics and warm studio light",
                        DomposableModifier
                            .width(
                                (
                                    if (wide) {
                                        (
                                            size.width -
                                                80
                                        ) *
                                            .62f
                                    } else {
                                        size.width - inset * 2
                                    }
                                ).coerceAtLeast(1f).dp,
                            ).height((if (wide) 330 else 240).dp),
                        onArtworkLoaded,
                        DomposableImageScale.Crop,
                    )
                }
                if (wide) {
                    DomposableRow(gap = 16.dp) {
                        copy()
                        photo()
                    }
                } else {
                    DomposableColumn(gap = 16.dp) {
                        photo()
                        copy()
                    }
                }
            }
            DomposableText(
                if (showHero) "Everyday favourites" else "Shop the collection",
                style = DomposableTextStyle.Title,
                headingLevel = 2,
            )
            DomposableFlowRow(gap = 12.dp) {
                DomposableTextField(
                    state.search,
                    {
                        onAction(CatalogAction.Search(it))
                    },
                    "Search products",
                    modifier =
                        DomposableModifier
                            .width(
                                240.dp,
                            ).testId("shop-search"),
                    purpose = DomposableInputPurpose.Search,
                )
                DomposableSwitch(checked = state.priceAscending, onCheckedChange = {
                    onAction(CatalogAction.Sort(it))
                }, label = "Price: low to high")
            }
            DomposableFlowRow(gap = 8.dp) {
                listOf("All", "Home", "Travel").forEach { category ->
                    DomposableChip(
                        label = category,
                        onClick = { onAction(CatalogAction.Category(category)) },
                        selected =
                            state.category == category,
                    )
                }
            }
            if (state.notice.isNotEmpty()) DomposableBanner(message = state.notice)
            DomposableText(
                "${state.visibleProducts.size} thoughtfully chosen pieces",
                style = DomposableTextStyle.Label,
            )
            if (state.visibleProducts.isEmpty()) {
                DomposableText("No products match your search.")
                DomposableButton("Clear filters", { onAction(CatalogAction.ClearFilters) })
            }
            DomposableFlowRow(gap = 16.dp) {
                state.visibleProducts.forEach { product ->
                    key(product.id) {
                        DomposableCard(DomposableModifier.width(cardWidth)) {
                            DomposableColumn(DomposableModifier.padding(16.dp).fillMaxWidth(), gap = 8.dp) {
                                artwork(
                                    product.image,
                                    product.name,
                                    DomposableModifier.fillMaxWidth().height(220.dp),
                                    null,
                                    DomposableImageScale.Fit,
                                )
                                DomposableText(product.category.uppercase(), style = DomposableTextStyle.Label)
                                DomposableText(product.name, style = DomposableTextStyle.Title, headingLevel = 3)
                                DomposableText(shopPrice(product.cents))
                                DomposableFlowRow(gap = 8.dp) {
                                    DomposableButton("View details", {
                                        onProduct(product.id)
                                    }, DomposableModifier.contentDescription("View ${product.name}"))
                                    DomposableButton("Add to bag", {
                                        onAction(CatalogAction.Add(product.id))
                                    }, DomposableModifier.contentDescription("Add ${product.name} to bag"))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ProductContent(
    state: ProductState,
    artwork: ShopArtwork,
    onAdd: () -> Unit,
    onBrowse: () -> Unit,
    onCart: () -> Unit,
) {
    val product = state.product
    DomposableColumn(
        DomposableModifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll()
            .padding(24.dp),
        gap = 16.dp,
    ) {
        DomposableButton("Back to collection", onBrowse)
        if (product == null) {
            DomposableText("This product is unavailable.")
        } else {
            DomposableText(product.category.uppercase(), style = DomposableTextStyle.Label)
            DomposableText(product.name, style = DomposableTextStyle.Title, headingLevel = 2)
            artwork(
                product.image,
                product.name,
                DomposableModifier.fillMaxWidth().height(300.dp),
                null,
                DomposableImageScale.Fit,
            )
            DomposableText(shopPrice(product.cents), style = DomposableTextStyle.Title)
            DomposableText(product.description)
            DomposableText("In stock · Dispatches in 2–3 working days", style = DomposableTextStyle.Label)
            DomposableFlowRow(gap = 12.dp) {
                DomposableButton("Add to bag", { onAdd() })
                DomposableButton("View bag", onCart)
            }
            if (state.notice.isNotEmpty()) DomposableBanner(message = state.notice)
            DomposableDivider()
            DomposableText("The details", style = DomposableTextStyle.Title, headingLevel = 3)
            DomposableText(
                "Designed to be used every day. Carefully chosen materials and simple, " +
                    "useful details. This is a fictional product in an open source example.",
            )
        }
    }
}
