package com.rafarg.ecogardengame.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * --- ANDROID-SPECIFIC IMPLEMENTATION (actual) ---
 * This file provides the real implementation for the 'vibrate' function
 * defined in commonMain. It interacts directly with Android's hardware APIs.
 */

private var vibrator: Vibrator? = null

/**
 * Initialization function for the Vibrator service.
 * In Android, hardware services are accessed via the 'Context'.
 */
fun initVibrator(context: Context) {
    /**
     * --- ANDROID API EVOLUTION ---
     * Android changed how the vibrator is accessed in API 31 (S).
     * We use a conditional check to support both modern and older devices.
     */
    vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Modern way: Access via VibratorManager
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            // Legacy way: Direct access to VIBRATOR_SERVICE
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
}

/**
 * Executes the vibration on the physical device.
 */
actual fun vibrate(milliseconds: Long) {
    if (milliseconds <= 0) return

    vibrator?.let {
        /**
         * --- VIBRATION EFFECTS (API 26+) ---
         * Since Android O, simple millisecond vibration was deprecated in favor of
         * 'VibrationEffect', which allows for more complex patterns.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            it.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // Support for very old devices (pre-Oreo)
            @Suppress("DEPRECATION")
            it.vibrate(milliseconds)
        }
    }
}
