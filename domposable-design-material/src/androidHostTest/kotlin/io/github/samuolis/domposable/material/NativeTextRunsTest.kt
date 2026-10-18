package io.github.samuolis.domposable.material

import androidx.compose.ui.graphics.Color
import io.github.samuolis.domposable.foundation.DomposableColor
import io.github.samuolis.domposable.foundation.DomposableFontWeight
import io.github.samuolis.domposable.ui.DomposableTextProps
import io.github.samuolis.domposable.ui.DomposableTextRun
import kotlin.test.Test
import kotlin.test.assertEquals

class NativeTextRunsTest {
    @Test
    fun runsStayOneTextValueWithInheritedAndExplicitStyles() {
        val props =
            DomposableTextProps(
                text = "Regular Emphasis Muted",
                runs =
                    listOf(
                        DomposableTextRun("Regular "),
                        DomposableTextRun("Emphasis", DomposableFontWeight(750), DomposableColor(0xFF123456)),
                        DomposableTextRun(" Muted", contentAlpha = 0.5f),
                    ),
            )
        val annotated = props.annotatedText(Color.Red)
        assertEquals(props.text, annotated.text)
        assertEquals(
            750,
            annotated.spanStyles[1]
                .item.fontWeight
                ?.weight,
        )
        assertEquals(Color(0xFF123456), annotated.spanStyles[1].item.color)
        assertEquals(Color.Red.copy(alpha = 0.5f), annotated.spanStyles[2].item.color)
        assertEquals(8, annotated.spanStyles[1].start)
    }
}
