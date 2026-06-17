package com.rafarg.ecogardengame.util

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper

/**
 * --- ANDROID-SPECIFIC IMPLEMENTATION (actual) ---
 * Manages the device's physical Proximity Sensor.
 * This is the sensor normally used to turn off the screen during phone calls.
 */

private var sensorManager: SensorManager? = null
private var proximityListener: SensorEventListener? = null

/**
 * Initializes the sensor manager with the Android System Service.
 */
fun initProximityDetector(context: Context) {
    sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
}

/**
 * Starts listening for proximity events.
 *
 * @param onNear Lambda triggered when something (like a hand) is detected near the sensor.
 */
actual fun startListeningForProximity(onNear: () -> Unit) {
    val proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY) ?: return

    // --- DEBOUNCE LOGIC ---
    // Sensors can trigger multiple times per second. We use a cooldown to ensure
    // a "hand wave" only counts as one harvest event in the game.
    var lastTriggerTime = 0L
    val mainHandler = Handler(Looper.getMainLooper())

    proximityListener =
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                // event.values[0] contains the distance in centimeters.
                val distance = event.values[0]

                /**
                 * --- HARDWARE VARIATION HANDLING ---
                 * Different phones have different sensor ranges.
                 * Some return 0.0 or 5.0, others have a continuous range.
                 * We consider "near" anything significantly closer than the sensor's maximum range.
                 */
                val isNear = distance < proximitySensor.maximumRange || distance == 0f

                if (isNear) {
                    val now = System.currentTimeMillis()
                    if (now - lastTriggerTime > 300) { // 300ms Cooldown
                        lastTriggerTime = now

                        /**
                         * --- THREAD SAFETY (Main Thread) ---
                         * Sensors run on a background hardware thread.
                         * We MUST use a Handler to send the callback to the Main Thread
                         * so Compose can update the UI safely without crashing.
                         */
                        mainHandler.post {
                            onNear()
                        }
                    }
                }
            }

            override fun onAccuracyChanged(
                sensor: Sensor?,
                accuracy: Int,
            ) {}
        }

    sensorManager?.registerListener(
        proximityListener,
        proximitySensor,
        SensorManager.SENSOR_DELAY_NORMAL,
    )
}

/**
 * Unregisters the listener to save battery power.
 */
actual fun stopListeningForProximity() {
    proximityListener?.let {
        sensorManager?.unregisterListener(it)
    }
    proximityListener = null
}
