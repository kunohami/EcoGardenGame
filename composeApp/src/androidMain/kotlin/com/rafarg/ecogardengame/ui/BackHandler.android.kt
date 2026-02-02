package com.rafarg.ecogardengame.ui

import androidx.compose.runtime.Composable

/**
 * --- ANDROID-SPECIFIC IMPLEMENTATION (actual) ---
 * Manages the physical back button (or gesture) on Android devices.
 * 
 * In Kotlin Multiplatform, handling the back button is tricky because 
 * iOS doesn't have a standard system back button like Android.
 */
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    /**
     * We delegate the functionality to the native Android Compose BackHandler.
     * This ensures that when the user swipes from the edge or taps back, 
     * our custom 'onBack' logic (like closing a sub-menu) is executed.
     */
    androidx.activity.compose.BackHandler(enabled, onBack)
}
