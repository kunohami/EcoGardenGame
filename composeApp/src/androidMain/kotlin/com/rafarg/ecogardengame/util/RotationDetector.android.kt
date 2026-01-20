package com.rafarg.ecogardengame.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

private var sensorManager: SensorManager? = null
private var rotationListener: SensorEventListener? = null

fun initRotationDetector(context: Context) {
    sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
}

actual fun startListeningForRotation(onRotationChanged: (Float) -> Unit) {
    val rotationMatrix = FloatArray(9)
    val orientationAngles = FloatArray(3)

    rotationListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return
            
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                
                // Azimuth is orientationAngles[0] in radians (-PI to PI)
                var azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                
                // Normalize to 0-360
                if (azimuthDegrees < 0) {
                    azimuthDegrees += 360f
                }
                
                onRotationChanged(azimuthDegrees)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    sensorManager?.registerListener(
        rotationListener,
        sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
        SensorManager.SENSOR_DELAY_GAME
    )
}

actual fun stopListeningForRotation() {
    rotationListener?.let {
        sensorManager?.unregisterListener(it)
    }
    rotationListener = null
}
