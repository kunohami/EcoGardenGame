package com.rafarg.ecogardengame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val prefs = remember { createDataStore(applicationContext) }
            App(prefs = prefs)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    // We cannot create a real DataStore in preview easily without context, 
    // but for preview purposes we might need a fake or modify App to be previewable without params
    // or just leave it broken in preview for now if we don't have a stub.
    // For now, let's just instantiate with a context if possible or comment it out.
    // Ideally, App should take state, not DataStore, to be preview friendly.
    // But refactoring that way might be too much change right now.
    // We will comment this out or just leave it empty.
}