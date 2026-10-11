package example.shop.catalog.api

import androidx.compose.runtime.staticCompositionLocalOf
import io.github.samuolis.domposable.navigation3.DomposableNavigator

/** Typed application navigation; the sample chooses its router without exposing it in screen args. */
val LocalShopNavigator = staticCompositionLocalOf<DomposableNavigator<ShopRoute>> {
    error("Provide LocalShopNavigator at the application root")
}

val LocalShopArtwork = staticCompositionLocalOf<ShopArtwork> {
    error("Provide LocalShopArtwork at the application root")
}

/** Optional capture hook used by the demo's platform screenshot hosts. */
val LocalShopArtworkLoaded = staticCompositionLocalOf<(() -> Unit)?> { null }
