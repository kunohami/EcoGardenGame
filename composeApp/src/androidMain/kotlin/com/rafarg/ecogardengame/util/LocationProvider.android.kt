package com.rafarg.ecogardengame.util

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * --- ANDROID-SPECIFIC IMPLEMENTATION (actual) ---
 * Provides access to the device's physical location using Google Play Services.
 * This is used to fetch the local weather data for the Garden.
 */

@Composable
actual fun rememberLocationProvider(): LocationProvider {
    // In Compose, we access the Android Context using LocalContext.
    val context = LocalContext.current
    // 'remember' ensures we don't recreate the provider object on every UI recomposition.
    return remember { AndroidLocationProvider(context) }
}

/**
 * Implementation of the [LocationProvider] interface for the Android platform.
 *
 * --- OOP PRINCIPLE: INTERFACE IMPLEMENTATION ---
 * This class implements the common interface defined in commonMain,
 * allowing the UI to remain platform-agnostic.
 */
class AndroidLocationProvider(private val context: Context) : LocationProvider {
    /**
     * --- GOOGLE PLAY SERVICES: FUSED LOCATION ---
     * The FusedLocationProviderClient is the recommended Android API for location.
     * It intelligently combines GPS, Wi-Fi, and Cell data to save battery.
     */
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Requests the current location from the system.
     *
     * @param onLocationReceived Callback invoked when location data is available or fails.
     */
    @SuppressLint("MissingPermission") // Permissions are handled in the UI layer before calling this.
    override fun requestLocation(onLocationReceived: (LocationCoordinates?) -> Unit) {
        // We use PRIORITY_BALANCED_POWER_ACCURACY because we don't need "block-level" precision
        // for weather; city-level is enough and much faster/cheaper on battery.
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
