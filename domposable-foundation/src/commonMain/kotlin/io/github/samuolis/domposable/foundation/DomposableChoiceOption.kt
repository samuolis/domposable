package io.github.samuolis.domposable.foundation

/** A choice keeps its typed identity when labels change or options are reordered. */
data class DomposableChoiceOption<T>(
    val value: T,
    val label: String,
    val enabled: Boolean = true,
    val testTag: String? = null,
)
