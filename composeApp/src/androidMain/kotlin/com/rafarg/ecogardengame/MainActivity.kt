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

class MainActivity : ComponentActivity() {
    
    private lateinit var authRepository: AndroidAuthRepository
    private lateinit var viewModel: GameViewModel

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(Exception::class.java)
                if (account != null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        authRepository.handleSignInResult(account)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize Firebase on Android
        Firebase.initialize(this)

        initVibrator(this)
        initShakeDetector(this)
        initRotationDetector(this)
        initProximityDetector(this)

        authRepository = AndroidAuthRepository(this)

        setContent {
            val prefs = remember { createDataStore(applicationContext) }
            // We need to pass the repository to the App/ViewModel
            App(prefs = prefs, authRepository = authRepository, onGoogleSignIn = {
                googleSignInLauncher.launch(authRepository.signInIntent)
            })
        }
    }
}
