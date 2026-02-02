package com.rafarg.ecogardengame.model

/**
 * Enum representing the primary navigation destinations in the application.
 * @property title The display name of the screen (used for accessibility or simple UI labels).
 * @property showInBottomBar Whether this screen should be visible in the main navigation bar.
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
