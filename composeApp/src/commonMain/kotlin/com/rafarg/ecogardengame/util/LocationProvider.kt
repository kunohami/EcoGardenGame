package com.rafarg.ecogardengame.util

import androidx.compose.runtime.Composable

data class LocationCoordinates(val latitude: Double, val longitude: Double)

@Composable
expect fun rememberLocationProvider(): LocationProvider

interface LocationProvider {
    fun requestLocation(onLocationReceived: (LocationCoordinates?) -> Unit)
}
