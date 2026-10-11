package io.github.samuolis.domposable.adaptive

import android.content.Context
import android.database.ContentObserver
import android.hardware.input.InputManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.InputDevice
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable internal actual fun reducedMotionPreference(): Boolean {
    val resolver = LocalContext.current.contentResolver

    fun read() = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    var reduced by remember(resolver) { mutableStateOf(read()) }
    DisposableEffect(resolver) {
        val observer =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    reduced = read()
                }
            }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return reduced
}

@Composable internal actual fun precisePointerPreference(): Boolean {
    val manager = LocalContext.current.getSystemService(Context.INPUT_SERVICE) as InputManager

    fun read() =
        manager.inputDeviceIds.any { id ->
            manager.getInputDevice(id)?.let { device ->
                device.supportsSource(InputDevice.SOURCE_MOUSE) || device.supportsSource(InputDevice.SOURCE_TOUCHPAD)
            } == true
        }
    var precise by remember(manager) { mutableStateOf(read()) }
    DisposableEffect(manager) {
        val listener =
            object : InputManager.InputDeviceListener {
                override fun onInputDeviceAdded(deviceId: Int) {
                    precise = read()
                }

                override fun onInputDeviceRemoved(deviceId: Int) {
                    precise = read()
                }

                override fun onInputDeviceChanged(deviceId: Int) {
                    precise = read()
                }
            }
        manager.registerInputDeviceListener(listener, Handler(Looper.getMainLooper()))
        onDispose { manager.unregisterInputDeviceListener(listener) }
    }
    return precise
}
