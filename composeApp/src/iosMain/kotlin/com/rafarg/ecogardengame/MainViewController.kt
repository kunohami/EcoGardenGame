package com.rafarg.ecogardengame

import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.runtime.remember

fun MainViewController() = ComposeUIViewController {
    App(
        prefs = remember { createDataStore() }
    )
}