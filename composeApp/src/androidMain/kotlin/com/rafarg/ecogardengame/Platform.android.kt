package com.rafarg.ecogardengame.util

import android.os.Build

/**
 * --- ANDROID-SPECIFIC IMPLEMENTATION (actual) ---
 * Provides metadata about the current Android device to the common code.
 */
class AndroidPlatform : Platform {
    /**
     * Returns the name of the OS and the SDK version.
     * Example: "Android 33"
     */
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

/**
 * The actual function that instantiates the Android version of the Platform interface.
 * This is called by commonMain code through the 'expect' declaration.
 * 
 * NOTE: The package must match exactly the one in commonMain (com.rafarg.ecogardengame.util).
 */
actual fun getPlatform(): Platform = AndroidPlatform()
