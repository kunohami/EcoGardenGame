package com.rafarg.ecogardengame.util

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * --- ANDROID-SPECIFIC IMPLEMENTATION (actual) ---
 * Detects device shake gestures using the physical Accelerometer.
 */

private var sensorManager: SensorManager? = null
private var shakeListener: SensorEventListener? = null

fun initShakeDetector(context: Context) {
    sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
}

actual fun startListeningForShake(onShake: () -> Unit) {
    var lastShakeTime: Long = 0
    // Logic Tuning: 12.0f is slightly above gravity, requiring a distinct "jerk" of the phone.
    val shakeThreshold = 12.0f
    val minTimeBetweenShakes = 500L

    shakeListener =
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                /**
                 * --- SENSOR DATA PROCESSING ---
                 * The Accelerometer gives us force in 3 axes (X, Y, Z).
                 * We calculate the "G-Force" total using the Euclidean Norm (Pythagoras in 3D).
                 */
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    // Subtract Earth's gravity so we only measure the user's manual force.
                    val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH

                    // If the movement force is stronger than our threshold...
                    if (acceleration > shakeThreshold) {
                        val now = System.currentTimeMillis()
                        // Cooldown logic to prevent multiple detections for a single physical shake.
                        if (now - lastShakeTime > minTimeBetweenShakes) {
                            lastShakeTime = now
                            // Notify the common code (Garlic.kt)
                            onShake()
                        }
                    }
                }
            }

            override fun onAccuracyChanged(
                sensor: Sensor?,
                accuracy: Int,
            ) {}
        }

    // Register the listener with 'DELAY_UI' to balance responsiveness and battery life.
    sensorManager?.registerListener(
        shakeListener,
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        SensorManager.SENSOR_DELAY_UI,
    )
}

actual fun stopListeningForShake() {
    shakeListener?.let {
        sensorManager?.unregisterListener(it)
    }
    shakeListener = null
}
