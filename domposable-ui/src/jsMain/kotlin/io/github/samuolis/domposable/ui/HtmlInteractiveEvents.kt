package io.github.samuolis.domposable.ui

import org.w3c.dom.Element
import org.w3c.dom.events.Event

private const val INTERACTIVE_CONTROL_SELECTOR =
    "a[href],button,input,select,textarea,label,summary,[contenteditable='true']," +
        "[role=button],[role=link],[role=checkbox],[role=radio],[role=switch],[role=menuitem],[role=tab]"

internal fun Event.belongsToCurrentInteractiveControl(): Boolean {
    val targetElement = target as? Element ?: return false
    return targetElement.closest(INTERACTIVE_CONTROL_SELECTOR) == currentTarget
}
