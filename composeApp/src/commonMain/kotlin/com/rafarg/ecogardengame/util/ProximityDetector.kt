package com.rafarg.ecogardengame.util

expect fun startListeningForProximity(onNear: () -> Unit)
expect fun stopListeningForProximity()
