package io.github.samuolis.domposable.demo

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.filters.SdkSuppress
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableSurface
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxWidth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@SdkSuppress(minSdkVersion = 26)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CollectionVisualTest {
    @get:Rule val compose = createComposeRule()

    @Test fun measuredCollections() = collectionCapture("collection-measured", "Measured column")

    @Test fun horizontalCollections() = collectionCapture("collection-row", "Horizontal row")

    @Test fun gridCollections() = collectionCapture("collection-grid", "Responsive grid")

    @Test fun tableCollections() = collectionCapture("collection-table", "Semantic table")

    private fun collectionCapture(
        name: String,
        mode: String,
    ) {
        compose.setContent {
            DomposableMaterialTheme {
                DomposableSurface(DomposableModifier.fillMaxWidth().fillMaxHeight()) { AdvancedCollections(mode) }
            }
        }
        compose.waitForIdle()
        save(name)
    }

    private fun save(name: String) {
        val target = File(System.getProperty("domposable.screenshots"), "android/$name.png")
        target.parentFile!!.mkdirs()
        target.outputStream().use {
            compose
                .onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
