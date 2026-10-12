package io.github.samuolis.domposable.navigation3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotMutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.runtime.toLong
import io.github.samuolis.domposable.screens.DomposableEntryLifecycle
import io.github.samuolis.domposable.screens.DomposableEntryScope
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import org.w3c.dom.HTMLElement

private fun Any?.savedJson(): JsonElement =
    when (this) {
        null -> {
            JsonNull
        }

        is SnapshotMutableState<*> -> {
            buildJsonObject {
                put("mutableState", value.savedJson())
                when (this@savedJson) {
                    is MutableIntState -> put("stateType", "int")
                    is MutableLongState -> put("stateType", "long")
                    is MutableFloatState -> put("stateType", "float")
                    is MutableDoubleState -> put("stateType", "double")
                }
                put(
                    "policy",
                    when (policy) {
                        structuralEqualityPolicy<Any?>() -> "structural"
                        referentialEqualityPolicy<Any?>() -> "referential"
                        neverEqualPolicy<Any?>() -> "never"
                        else -> error("Use a Saver for custom mutation policies")
                    },
                )
            }
        }

        is String -> {
            JsonPrimitive(this)
        }

        is Boolean -> {
            JsonPrimitive(this)
        }

        is Number -> {
            buildJsonObject {
                put(
                    "numericType",
                    when (this@savedJson) {
                        is Long -> "long"
                        is Float -> "float"
                        is Double -> "double"
                        is Short -> "short"
                        is Byte -> "byte"
                        else -> "int"
                    },
                )
                put("value", this@savedJson.toString())
            }
        }

        is List<*> -> {
            JsonArray(map { it.savedJson() })
        }

        is Map<*, *> -> {
            buildJsonObject { put("map", JsonObject(entries.associate { it.key as String to it.value.savedJson() })) }
        }

        else -> {
            error("Browser history cannot serialize ${this::class}; use portable saveables")
        }
    }

private fun JsonElement.savedValue(): Any? =
    when (this) {
        JsonNull -> {
            null
        }

        is JsonArray -> {
            map { it.savedValue() }
        }

        is JsonObject -> {
            if ("mutableState" in
                this
            ) {
                restoredMutableState()
            } else if (size == 1 && "map" in this) {
                getValue("map").jsonObject.mapValues { it.value.savedValue() }
            } else if (size == 2 && this["numericType"] is JsonPrimitive && this["value"] is JsonPrimitive) {
                val value = getValue("value").jsonPrimitive.content
                when (getValue("numericType").jsonPrimitive.content) {
                    "float" -> value.toFloat()
                    "double" -> value.toDouble()
                    "long" -> value.toLong()
                    "short" -> value.toShort()
                    "byte" -> value.toByte()
                    else -> value.toInt()
                }
            } else {
                mapValues { it.value.savedValue() }
            }
        }

        is JsonPrimitive -> {
            if (isString) content else booleanOrNull ?: intOrNull ?: longOrNull ?: doubleOrNull
        }
    }

private fun JsonObject.restoredMutableState(): MutableState<*> {
    val value = getValue("mutableState").savedValue()
    return when (get("stateType")?.jsonPrimitive?.content) {
        "int" -> {
            mutableIntStateOf(value as Int)
        }

        "long" -> {
            mutableLongStateOf(value as Long)
        }

        "float" -> {
            mutableFloatStateOf(value as Float)
        }

        "double" -> {
            mutableDoubleStateOf(value as Double)
        }

        else -> {
            mutableStateOf(
                value,
                when (get("policy")?.jsonPrimitive?.content) {
                    "referential" -> referentialEqualityPolicy()
                    "never" -> neverEqualPolicy()
                    else -> structuralEqualityPolicy()
                },
            )
        }
    }
}

private class BrowserBinding<R : Any>(
    val stack: SnapshotStateList<DomposableEntryScope<R>>,
    var codec: DomposableRouteCodec<R>,
    val urls: DomposableBrowserUrls,
) {
    var replaceEntry = false

    private fun routePath(): String =
        when (val mode = urls) {
            DomposableBrowserUrls.Hash -> {
                window.location.hash.removePrefix("#")
            }

            is DomposableBrowserUrls.Path -> {
                window.location.pathname
                    .removePrefix(mode.basePath.trimEnd('/'))
                    .ifEmpty {
                        "/"
                    }.trimEnd('/')
                    .ifEmpty { "/" }
            }
        }

    private fun url(route: R): String = codec.urlFor(route, urls)

    private val snapshots = mutableMapOf<String, String>()
    private var currentId = stack.last().id
    private var previousIds = stack.map { it.id }
    private var applyingHistory = false

    private fun snapshot(): String =
        buildJsonObject {
            put("domposable", 1)
            put(
                "entries",
                JsonArray(
                    stack.map { entry ->
                        buildJsonObject {
                            put("id", entry.id)
                            put("route", codec.encode(entry.route))
                            put(
                                "saved",
                                JsonObject(
                                    entry.saveableStateRegistry.performSave().mapValues { (_, values) ->
                                        JsonArray(values.map { it.savedJson() })
                                    },
                                ),
                            )
                        }
                    },
                ),
            )
            put("scrollX", window.scrollX)
            put("scrollY", window.scrollY)
            put(
                "elementScroll",
                buildJsonObject {
                    val elements = document.querySelectorAll("[data-entry-id],.d-scaffold-body")
                    for (index in 0 until elements.length) {
                        val element = elements.item(index) as HTMLElement
                        put(
                            element.getAttribute("data-entry-id") ?: "shell",
                            JsonArray(listOf(JsonPrimitive(element.scrollLeft), JsonPrimitive(element.scrollTop))),
                        )
                    }
                },
            )
        }.toString()

    private fun readSnapshot(value: String?): JsonObject? =
        try {
            if (value == null) {
                null
            } else {
                Json
                    .parseToJsonElement(value)
                    .jsonObject
                    .takeIf { it["domposable"]?.jsonPrimitive?.intOrNull == 1 }
            }
        } catch (_: Exception) {
            null
        }

    fun initialize() {
        if (urls is DomposableBrowserUrls.Path) {
            val base = urls.basePath.trimEnd('/')
            require(window.location.pathname == base || window.location.pathname.startsWith("$base/")) {
                "URL is outside the configured application base"
            }
        }
        val state = readSnapshot(browserHistoryState())
        if (state != null) {
            restore(state)
        } else {
            codec.decode(routePath())?.let { route ->
                stack.clear()
                stack.add(DomposableEntryScope(route))
            }
        }
        currentId = stack.last().id
        previousIds = stack.map { it.id }
        val value = snapshot()
        snapshots[currentId] = value
        replaceBrowserHistoryState(value, url(stack.last().route))
        applyingHistory = false
    }

    private fun restore(state: JsonObject) {
        val entries = state["entries"]?.jsonArray ?: return
        val live = stack.associateBy { it.id }
        val restored =
            entries.mapNotNull { item ->
                val data = item.jsonObject
                val route = codec.decode(data.getValue("route").jsonPrimitive.content) ?: return@mapNotNull null
                val id = data.getValue("id").jsonPrimitive.content
                val saved =
                    data["saved"]
                        ?.jsonObject
                        ?.mapValues { (_, value) ->
                            value.jsonArray.map { it.savedValue() }
                        }.orEmpty()
                live[id]?.takeIf { it.lifecycle != DomposableEntryLifecycle.Destroyed && it.route == route }
                    ?: DomposableEntryScope(route, id, saved)
            }
        if (restored.isEmpty()) return
        applyingHistory = true
        stack.clear()
        stack.addAll(restored)
        currentId = stack.last().id
        window.requestAnimationFrame {
            window.scrollTo(
                state["scrollX"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                state["scrollY"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
            )
            val elements = document.querySelectorAll("[data-entry-id],.d-scaffold-body")
            for (index in 0 until elements.length) {
                val element = elements.item(index) as HTMLElement
                val saved =
                    state["elementScroll"]?.jsonObject?.get(element.getAttribute("data-entry-id") ?: "shell")?.jsonArray
                        ?: continue
                element.scrollLeft = saved[0].jsonPrimitive.double
                element.scrollTop = saved[1].jsonPrimitive.double
            }
        }
    }

    fun changed(ids: List<String>) {
        if (stack.isEmpty() || ids != stack.map { it.id }) return
        val id = stack.last().id
        if (applyingHistory) {
            applyingHistory = false
            currentId = id
            previousIds = ids
            return
        }
        // An ordinary stack pop traverses its existing browser entry.
        if (previousIds.size > 1 && ids == previousIds.dropLast(1)) {
            currentId = id
            previousIds = ids
            window.history.back()
            return
        }
        if (id != currentId) {
            snapshots[currentId]?.let { replaceBrowserHistoryState(it, null) }
            val value = snapshot()
            if (replaceEntry) {
                replaceBrowserHistoryState(value, url(stack.last().route))
            } else {
                pushBrowserHistoryState(value, url(stack.last().route))
            }
            currentId = id
        }
        saveCurrent()
        previousIds = ids
    }

    fun saveCurrent() {
        if (stack.isEmpty() || stack.last().id != currentId) return
        val value = snapshot()
        snapshots[currentId] = value
        replaceBrowserHistoryState(value, null)
    }

    fun pop() {
        val state = readSnapshot(browserHistoryState())
        if (state != null) {
            val top =
                state["entries"]
                    ?.jsonArray
                    ?.lastOrNull()
                    ?.jsonObject
                    ?.get("id")
                    ?.jsonPrimitive
                    ?.content
            restore(readSnapshot(snapshots[top]) ?: state)
        } else {
            codec.decode(routePath())?.let { route ->
                applyingHistory = true
                stack.clear()
                stack.add(DomposableEntryScope(route))
                currentId = stack.last().id
            }
        }
    }
}

@Composable
internal fun <R : Any> BrowserHistoryBinding(
    backStack: SnapshotStateList<DomposableEntryScope<R>>,
    codec: DomposableRouteCodec<R>,
    urls: DomposableBrowserUrls,
) {
    val binding = remember(backStack, urls) { BrowserBinding(backStack, codec, urls) }
    binding.codec = codec
    binding.replaceEntry = LocalReplaceBrowserEntry.current
    DisposableEffect(binding) {
        binding.initialize()
        val pop: (org.w3c.dom.events.Event) -> Unit = { binding.pop() }
        val save: (org.w3c.dom.events.Event) -> Unit = { binding.saveCurrent() }
        val input: (org.w3c.dom.events.Event) -> Unit = { window.requestAnimationFrame { binding.saveCurrent() } }
        window.addEventListener("click", save, true)
        window.addEventListener("keydown", save, true)
        window.addEventListener("input", input)
        window.addEventListener("change", input)
        window.addEventListener("popstate", pop)
        window.addEventListener("scroll", save, true)
        window.addEventListener("beforeunload", save)
        onDispose {
            window.removeEventListener("click", save, true)
            window.removeEventListener("keydown", save, true)
            window.removeEventListener("input", input)
            window.removeEventListener("change", input)
            binding.saveCurrent()
            window.removeEventListener("popstate", pop)
            window.removeEventListener("scroll", save, true)
            window.removeEventListener("beforeunload", save)
        }
    }
    val entryIds = backStack.map { it.id }
    SideEffect { binding.changed(entryIds) }
}

internal expect fun browserHistoryState(): String?

internal expect fun replaceBrowserHistoryState(
    value: String,
    url: String?,
)

internal expect fun pushBrowserHistoryState(
    value: String,
    url: String,
)
