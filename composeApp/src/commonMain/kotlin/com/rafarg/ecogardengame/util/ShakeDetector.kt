package com.rafarg.ecogardengame.util

/**
 * Platform-agnostic interface for detecting a device shake gesture.
 * Primarily used by the Garlic vegetable to trigger the "Shake to Harvest" modifier.
 * Implementation resides in platform-specific modules (e.g., ShakeDetector.android.kt).
 */
expect fun startListeningForShake(onShake: () -> Unit)

/**
 * Stops the accelerometer listeners to save battery when the shake gesture is no longer needed.
 */
expect fun stopListeningForShake()
