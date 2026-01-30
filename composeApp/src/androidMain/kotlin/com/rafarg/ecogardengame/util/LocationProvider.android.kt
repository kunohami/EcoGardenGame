package com.rafarg.ecogardengame.util

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@Composable
actual fun rememberLocationProvider(): LocationProvider {
    val context = LocalContext.current
    return remember { AndroidLocationProvider(context) }
}

class AndroidLocationProvider(private val context: Context) : LocationProvider {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override fun requestLocation(onLocationReceived: (LocationCoordinates?) -> Unit) {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    onLocationReceived(LocationCoordinates(location.latitude, location.longitude))
                } else {
                    onLocationReceived(null)
                }
            }
            .addOnFailureListener {
                onLocationReceived(null)
            }
    }
}
