package com.rafarg.ecogardengame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.rafarg.ecogardengame.auth.AndroidAuthRepository
import com.rafarg.ecogardengame.ui.App
import com.rafarg.ecogardengame.util.initVibrator
import com.rafarg.ecogardengame.util.initShakeDetector
import com.rafarg.ecogardengame.util.initRotationDetector
import com.rafarg.ecogardengame.util.initProximityDetector
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * --- ANDROID ENTRY POINT (The Activity) ---
 * This is the first class that runs when the user taps the app icon on Android.
 * It inherits from ComponentActivity to provide support for Jetpack Compose.
 */
class MainActivity : ComponentActivity() {
    
    private lateinit var authRepository: AndroidAuthRepository

    /**
     * --- ANDROID INTERACTION: ACTIVITY RESULT API ---
     * This modern API is used to launch other apps (like Google Sign-In) 
     * and receive data back safely without managing request codes manually.
     */
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // User signed in successfully!
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(Exception::class.java)
                if (account != null) {
                    // Process the result in a Coroutine to avoid blocking the UI.
                    CoroutineScope(Dispatchers.Main).launch {
                        authRepository.handleSignInResult(account)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * ANDROID LIFECYCLE: onCreate
     * This method initializes the app's components.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Show the native Android splash screen while the app boots up.
        installSplashScreen()
        // Make the app occupy the full screen (behind status and nav bars).
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        /**
         * --- SERVICE INITIALIZATION ---
         * We initialize all hardware detectors. On Android, these need 
         * the 'Context' (this Activity) to talk to the hardware.
         */
        Firebase.initialize(this) // Initialize Firebase for cloud saves
        initVibrator(this)
        initShakeDetector(this)
        initRotationDetector(this)
        initProximityDetector(this)

        authRepository = AndroidAuthRepository(this)

        // Set the UI content using Jetpack Compose.
        setContent {
            // 'remember' creates the DataStore once and keeps it alive.
            val prefs = remember { createDataStore(applicationContext) }
            
            /**
             * --- THE APP COMPOSABLE ---
             * We launch the shared UI (commonMain) and pass the 
             * platform-specific dependencies (prefs, auth).
             */
            App(
                prefs = prefs, 
                authRepository = authRepository, 
                onGoogleSignIn = {
                    // Start the Google Login process.
                    googleSignInLauncher.launch(authRepository.signInIntent)
                }
            )
        }
    }
}
