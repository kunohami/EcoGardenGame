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
 * Screen for displaying player statistics and achievements.
 */
@Composable
fun StatsScreen(viewModel: GameViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Stats", style = MaterialTheme.typography.displayMedium)
    }
}
