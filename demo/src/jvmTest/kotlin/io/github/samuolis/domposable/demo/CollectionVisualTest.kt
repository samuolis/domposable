package io.github.samuolis.domposable.demo

import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import io.github.samuolis.domposable.material.DomposableMaterialTheme
import io.github.samuolis.domposable.ui.DomposableModifier
import io.github.samuolis.domposable.ui.DomposableSurface
import io.github.samuolis.domposable.ui.fillMaxHeight
import io.github.samuolis.domposable.ui.fillMaxWidth
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

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
        val target = File(System.getProperty("domposable.screenshots"), "desktop/$name.png")
        target.parentFile.mkdirs()
        ImageIO.write(compose.onRoot().captureToImage().toAwtImage(), "png", target)
    }
}
