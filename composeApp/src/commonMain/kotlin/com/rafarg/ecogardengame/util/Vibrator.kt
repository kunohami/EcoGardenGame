package com.rafarg.ecogardengame.util

/**
 * Platform-agnostic interface for triggering device vibration.
 * Used to provide haptic feedback during clicks, precision hits, and other gameplay events.
 * Implementation resides in platform-specific modules (e.g., Vibrator.android.kt).
 * 
 * @param milliseconds The duration of the vibration in milliseconds.
 */
expect fun vibrate(milliseconds: Long)
