package com.rafarg.ecogardengame.util

import android.content.Context
import android.hardware.Sensor
import android.content.Context.SENSOR_SERVICE
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

private var sensorManager: SensorManager? = null
private var shakeListener: SensorEventListener? = null

fun initShakeDetector(context: Context) {
    sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
}

actual fun startListeningForShake(onShake: () -> Unit) {
    var lastShakeTime: Long = 0
    val SHAKE_THRESHOLD = 12.0f
    val MIN_TIME_BETWEEN_SHAKES = 500L

    shakeListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
                if (acceleration > SHAKE_THRESHOLD) {
                    val now = System.currentTimeMillis()
                    if (now - lastShakeTime > MIN_TIME_BETWEEN_SHAKES) {
                        lastShakeTime = now
                        onShake()
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    sensorManager?.registerListener(
        shakeListener,
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        SensorManager.SENSOR_DELAY_UI
    )
}

actual fun stopListeningForShake() {
    shakeListener?.let {
        sensorManager?.unregisterListener(it)
    }
    shakeListener = null
}
