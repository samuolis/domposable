package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableTextAppearance
import org.jetbrains.compose.web.attributes.AttrsScope

/**
 * Applies the same theme typography and presentation as DomposableText to a custom HTML text host.
 * Roles resolve through the nearest theme's variables; explicit appearance values override them.
 * This adds no element and leaves content, events, line measurement and semantics to the caller.
 */
fun AttrsScope<*>.domposableTextAppearance(appearance: DomposableTextAppearance) {
    style { applyCatalogTextAppearance(appearance) }
}
