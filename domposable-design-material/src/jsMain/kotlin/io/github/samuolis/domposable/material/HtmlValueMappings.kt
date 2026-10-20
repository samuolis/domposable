package io.github.samuolis.domposable.material

import io.github.samuolis.domposable.foundation.DomposableAlignment
import io.github.samuolis.domposable.foundation.DomposableAutocomplete
import io.github.samuolis.domposable.ui.DomposableArrangement

internal fun DomposableAlignment.css() =
    when (this) {
        DomposableAlignment.Start -> "flex-start"
        DomposableAlignment.Center -> "center"
        DomposableAlignment.End -> "flex-end"
    }

internal fun DomposableArrangement.css() =
    when (this) {
        DomposableArrangement.Start -> "flex-start"
        DomposableArrangement.Center -> "center"
        DomposableArrangement.End -> "flex-end"
        DomposableArrangement.SpaceBetween -> "space-between"
        DomposableArrangement.SpaceAround -> "space-around"
        DomposableArrangement.SpaceEvenly -> "space-evenly"
    }

internal fun DomposableArrangement.distributesSpace() =
    this == DomposableArrangement.SpaceBetween ||
        this == DomposableArrangement.SpaceAround ||
        this == DomposableArrangement.SpaceEvenly

internal fun DomposableAutocomplete.htmlValue() =
    when (this) {
        DomposableAutocomplete.Off -> "off"
        DomposableAutocomplete.Email -> "email"
        DomposableAutocomplete.Phone -> "tel"
        DomposableAutocomplete.Username -> "username"
        DomposableAutocomplete.CurrentPassword -> "current-password"
        DomposableAutocomplete.NewPassword -> "new-password"
        DomposableAutocomplete.OneTimeCode -> "one-time-code"
    }
