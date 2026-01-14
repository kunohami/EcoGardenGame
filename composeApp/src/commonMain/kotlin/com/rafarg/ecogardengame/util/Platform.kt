package com.rafarg.ecogardengame.util

/**
 * Interface representing the current operating platform (Android, iOS, etc.).
 *
 * This is used to provide platform-specific information, such as the OS version, 
 * to the common code. It follows the Kotlin Multiplatform expect/actual pattern.
 */
interface Platform {
    val name: String
}

/**
 * Expected function to retrieve the platform-specific implementation of the Platform interface.
 *
 * How to use: Call getPlatform() from commonMain to access platform-specific strings or behaviors
 * defined in the respective androidMain and iosMain source sets.
 */
expect fun getPlatform(): Platform
