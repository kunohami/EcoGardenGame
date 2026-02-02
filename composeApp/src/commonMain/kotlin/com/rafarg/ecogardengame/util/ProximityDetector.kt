package com.rafarg.ecogardengame.util

/**
 * Platform-agnostic interface for using the device's proximity sensor.
 * Primarily used by the Broccoli vegetable to trigger the "Air Harvest" modifier, 
 * allowing the user to harvest by waving their hand near the sensor.
 */
expect fun startListeningForProximity(onNear: () -> Unit)

/**
 * Stops the proximity sensor listeners to save battery when the feature is not active.
 */
expect fun stopListeningForProximity()
