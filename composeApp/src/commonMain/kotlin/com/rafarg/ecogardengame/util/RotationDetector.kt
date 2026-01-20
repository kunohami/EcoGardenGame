package com.rafarg.ecogardengame.util

/**
 * Provides the current rotation of the device in degrees (0-360).
 * Usually representing the Azimuth/Yaw (compass direction).
 */
expect fun startListeningForRotation(onRotationChanged: (Float) -> Unit)
expect fun stopListeningForRotation()
