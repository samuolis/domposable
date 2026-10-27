package example.custom

import androidx.compose.runtime.Composable
import io.github.samuolis.domposable.foundation.DomposableButtonAppearance
import io.github.samuolis.domposable.foundation.DomposableShape
import io.github.samuolis.domposable.foundation.dp
import io.github.samuolis.domposable.ui.DomposableButton
import io.github.samuolis.domposable.ui.DomposableText

@Composable
fun BrandButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    DomposableButton(
        onClick = onClick,
        enabled = enabled,
        appearance = DomposableButtonAppearance(shape = DomposableShape.Rounded(12.dp)),
    ) {
        DomposableText(label)
    }
}
