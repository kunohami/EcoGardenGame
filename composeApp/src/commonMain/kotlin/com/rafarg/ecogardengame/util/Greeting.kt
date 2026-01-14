package com.rafarg.ecogardengame.util

/**
 * A simple utility class used to generate a greeting message.
 *
 * It demonstrates how to interact with the Platform utility to get device-specific strings.
 *
 * How to use:
 * val greeting = Greeting().greet()
 * println(greeting) // Outputs "Hello, Android 31!" or similar.
 */
class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}
