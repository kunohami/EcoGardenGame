package com.rafarg.ecogardengame.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * --- ANDROID-SPECIFIC IMPLEMENTATION (actual) ---
 * Provides device orientation (Azimuth) data using hardware sensors.
 * This is used for the "Apple" compass mechanic.
 */

private var sensorManager: SensorManager? = null
private var rotationListener: SensorEventListener? = null

/**
 * Initializes the sensor manager with the Android Context.
 */
fun initRotationDetector(context: Context) {
    sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
}

/**
 * Starts listening for orientation changes.
 *
 * --- SENSOR FUSION ---
 * Instead of raw Compass data, we use the ROTATION_VECTOR sensor.
 * This is a "virtual" sensor that combines Accelerometer, Gyroscope, and Magnetometer
 * to provide a stable, filtered direction without manual math.
 */
actual fun startListeningForRotation(onRotationChanged: (Float) -> Unit) {
    val rotationMatrix = FloatArray(9)
    val orientationAngles = FloatArray(3)

    rotationListener =
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    // 1. Convert raw sensor data into a Rotation Matrix
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    // 2. Extract Orientation (Azimuth, Pitch, Roll) from the matrix
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)

                    /**
                     * --- TRIGONOMETRY NORMALIZATION ---
                     * Azimuth is orientationAngles[0] in radians (-PI to PI).
                     * We convert it to degrees (0 to 360) for easier logic in the Apple game.
                     */
                    var azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()

                    // Keep the value positive: if it's -90, it becomes 270.
                    if (azimuthDegrees < 0) {
                        azimuthDegrees += 360f
                    }

                    // Pass the clean angle back to the commonMain logic.
                    onRotationChanged(azimuthDegrees)
                }
            }

            override fun onAccuracyChanged(
                sensor: Sensor?,
                accuracy: Int,
            ) {}
        }

    // Register with 'DELAY_GAME' for fast updates during gameplay.
    sensorManager?.registerListener(
        rotationListener,
        sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
        SensorManager.SENSOR_DELAY_GAME,
    )
}

/**
 * Stops the hardware listener to conserve battery when leaving the Apple screen.
 */
actual fun stopListeningForRotation() {
    rotationListener?.let {
        sensorManager?.unregisterListener(it)
    }
    rotationListener = null
}
