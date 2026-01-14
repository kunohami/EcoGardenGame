package com.rafarg.ecogardengame.model

/**
 * Enum representing the different screens in the application.
 */
enum class Screen(val title: String, val showInBottomBar: Boolean = true) {
    GAME("Game"),
    STORE("Store"),
    STATS("Stats"),
    PROFILE("Profile"),
    MISC("Misc"),
    SETTINGS("Settings", showInBottomBar = false),
    ABOUT("About", showInBottomBar = false)
}
