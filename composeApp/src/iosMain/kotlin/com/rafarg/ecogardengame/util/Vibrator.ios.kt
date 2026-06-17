package com.rafarg.ecogardengame.util

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

actual fun vibrate(milliseconds: Long) {
    if (milliseconds <= 0) return

    val style =
        when {
            milliseconds < 20 -> UIImpactFeedbackStyle.UIImpactFeedbackStyleLight
            milliseconds < 50 -> UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium
            else -> UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy
        }

    val generator = UIImpactFeedbackGenerator(style)
    generator.prepare()
    generator.impactOccurred()
}
