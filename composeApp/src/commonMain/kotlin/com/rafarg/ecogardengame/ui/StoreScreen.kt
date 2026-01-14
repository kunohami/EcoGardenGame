package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rafarg.ecogardengame.viewmodel.GameViewModel

/**
 * Screen for purchasing upgrades and new items.
 */
@Composable
fun StoreScreen(viewModel: GameViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Store", style = MaterialTheme.typography.displayMedium)
    }
}
