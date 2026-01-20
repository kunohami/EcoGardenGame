package com.rafarg.ecogardengame.util

import android.content.Context
import android.hardware.Sensor
import android.content.Context.SENSOR_SERVICE
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper

private var sensorManager: SensorManager? = null
private var proximityListener: SensorEventListener? = null

fun initProximityDetector(context: Context) {
    sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
}

actual fun startListeningForProximity(onNear: () -> Unit) {
    val proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY) ?: return
    
    // Use a simple time-based debounce for better compatibility
    var lastTriggerTime = 0L
    val mainHandler = Handler(Looper.getMainLooper())

    proximityListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return
            
            val distance = event.values[0]
            // On most Androids, 0.0 is near. 
            // We use a safe threshold: any value significantly lower than max range is "near"
            val isNear = distance < proximitySensor.maximumRange || distance == 0f
            
            if (isNear) {
                val now = System.currentTimeMillis()
                if (now - lastTriggerTime > 300) { // 300ms cooldown
                    lastTriggerTime = now
                    // Ensure callback runs on Main Thread for Compose safety
                    mainHandler.post {
                        onNear()
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    sensorManager?.registerListener(
        proximityListener,
        proximitySensor,
        SensorManager.SENSOR_DELAY_NORMAL
    )
}

actual fun stopListeningForProximity() {
    proximityListener?.let {
        sensorManager?.unregisterListener(it)
    }
    proximityListener = null
}
