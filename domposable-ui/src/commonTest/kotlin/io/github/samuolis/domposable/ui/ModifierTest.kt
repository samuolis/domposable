package io.github.samuolis.domposable.ui

import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableInsets
import io.github.samuolis.domposable.foundation.DomposableLayoutAxis
import io.github.samuolis.domposable.foundation.DomposableOffset
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ModifierTest {
    @Test fun paddingAndBackgroundRemainOrderedAndComposable() {
        val color = DomposableColor(0xFF4059AD)
        val chain = DomposableModifier.padding(8.dp).background(color).padding(16.dp)
        assertEquals(
            listOf(
                DomposableModifierOperation.Padding(DomposableInsets(8.dp)),
                DomposableModifierOperation.Background(color),
                DomposableModifierOperation.Padding(DomposableInsets(16.dp)),
            ),
            chain.operations,
        )
        assertEquals(chain.operations, DomposableModifier.then(chain).operations)
        assertTrue(DomposableModifier.operations.isEmpty())
        assertFailsWith<IllegalArgumentException> { DomposableModifier.alpha(2f) }
    }

    @Test fun extensionElementsKeepTheirOrderAcrossBuiltInChains() {
        data class BrandSurface(
            val token: String,
        ) : DomposableModifierElement
        val brand = BrandSurface("surface-raised")
        val modifier =
            DomposableModifier
                .padding(8.dp)
                .then(brand)
                .border(1.dp, DomposableColor(0xFF4059AD), DomposableShape.Rounded(12.dp))
                .then(DomposableModifier.zIndex(2f))

        assertEquals(
            listOf(
                DomposableModifierOperation.Padding(DomposableInsets(8.dp)),
                brand,
                DomposableModifierOperation.Border(
                    1.dp,
                    DomposableColor(0xFF4059AD),
                    DomposableShape.Rounded(12.dp),
                ),
                DomposableModifierOperation.ZIndex(2f),
            ),
            modifier.elements,
        )
        assertEquals(
            listOf(
                DomposableModifierOperation.Padding(DomposableInsets(8.dp)),
                DomposableModifierOperation.Border(
                    1.dp,
                    DomposableColor(0xFF4059AD),
                    DomposableShape.Rounded(12.dp),
                ),
                DomposableModifierOperation.ZIndex(2f),
            ),
            modifier.operations,
        )
    }

    @Test fun portableOperationsValidateInputsAndKeepScopeMeaning() {
        assertFailsWith<IllegalArgumentException> { DomposableModifier.aspectRatio(0f) }
        assertFailsWith<IllegalArgumentException> { DomposableModifier.weight(Float.NaN) }
        assertFailsWith<IllegalArgumentException> { DomposableModifier.offset(Float.POSITIVE_INFINITY, 0f) }
        assertEquals(
            DomposableModifierOperation.AxisAlignment(DomposableLayoutAxis.Horizontal, DomposableAlignment.End),
            DomposableModifier.align(DomposableLayoutAxis.Horizontal, DomposableAlignment.End).elements.single(),
        )
        assertEquals(
            DomposableModifierOperation.Offset(DomposableOffset(-4f, 6f)),
            DomposableModifier.offset(-4f, 6f).elements.single(),
        )
    }

    @Test fun shapeClickAndChildLayoutOperationsArePortableElements() {
        val state = DomposableScrollState(0)
        val shape = DomposableShape.Corners(topStart = 20.dp, topEnd = 20.dp)
        val modifier =
            DomposableModifier
                .minWidth(40.dp)
                .minHeight(12.dp)
                .maxHeight(240.dp)
                .aspectRatio(1.5f)
                .wrapContentSize(DomposableAlignment.End, DomposableAlignment.Center)
                .background(DomposableColor(0xFF112233), shape)
                .clip(shape)
                .border(1.dp, DomposableColor(0xFF112233), shape)
                .clickable(onClick = {}, role = DomposableClickRole.Link, enabled = false, onClickLabel = "open")
                .weight(2f, fill = false)
                .boxAlignment(DomposableAlignment.Start, DomposableAlignment.End)
                .verticalScroll(state)

        assertTrue(
            modifier.elements.contains(
                DomposableModifierOperation.ShapeBackground(
                    DomposableColor(0xFF112233),
                    shape,
                ),
            ),
        )
        val click = modifier.elements.filterIsInstance<DomposableModifierOperation.InteractiveClick>().single()
        assertEquals(DomposableClickRole.Link, click.role)
        assertEquals(false, click.enabled)
        assertEquals("open", click.label)
        assertEquals(state, (modifier.elements.last() as DomposableModifierOperation.StatefulScroll).state)
        assertEquals(12, modifier.elements.size)
    }
}
