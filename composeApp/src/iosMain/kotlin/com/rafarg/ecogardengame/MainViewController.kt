package com.rafarg.ecogardengame

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.rafarg.ecogardengame.ui.App

@Suppress("ktlint:standard:function-naming")
fun MainViewController() =
    ComposeUIViewController {
        App(
            prefs = remember { createDataStore() },
        )
    }
