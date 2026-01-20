package com.rafarg.ecogardengame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.rafarg.ecogardengame.ui.App
import com.rafarg.ecogardengame.util.initVibrator
import com.rafarg.ecogardengame.util.initShakeDetector
import com.rafarg.ecogardengame.util.initRotationDetector
import com.rafarg.ecogardengame.util.initProximityDetector

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initVibrator(this)
        initShakeDetector(this)
        initRotationDetector(this)
        initProximityDetector(this)

        setContent {
            val prefs = remember { createDataStore(applicationContext) }
            App(prefs = prefs)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
}