package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.stringResource

/**
 * StatsScreen provides a detailed breakdown of the player's progress.
 * It shows total clicks, lifetime earnings, and specific harvest counts for each crop.
 */
@Composable
fun StatsScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER ---
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onBack) {
                Text(stringResource(Res.string.back_label))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(stringResource(Res.string.stats_title), style = MaterialTheme.typography.displaySmall)
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // --- GENERAL SUMMARY CARDS ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Lifetime Clicks Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("👆 Clicks", style = MaterialTheme.typography.labelLarge)
                    Text("${viewModel.totalClicks}", style = MaterialTheme.typography.headlineSmall)
                }
            }

            // Money Statistics Card (Current vs Lifetime)
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🪙 ${stringResource(Res.string.stats_money)}", style = MaterialTheme.typography.labelLarge)
                    Text("${stringResource(Res.string.stats_now)}: ${viewModel.money}", style = MaterialTheme.typography.bodyMedium)
                    Text("${stringResource(Res.string.stats_total)}: ${viewModel.totalMoneyEarned}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- CROP BREAKDOWN ---
        Text(
            text = stringResource(Res.string.stats_fruits_harvested), 
            style = MaterialTheme.typography.titleLarge, 
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // LazyColumn is used here because the list of vegetables could grow long.
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(viewModel.itemsList) { item ->
                // Fetch current and total counts from the ViewModel maps
                val current = viewModel.fruitCounts[item.id] ?: 0
                val total = viewModel.totalFruitHarvested[item.id] ?: 0
                
                // ListItem is a Material 3 component perfect for simple rows
                ListItem(
                    headlineContent = { Text(stringResource(item.nameRes)) },
                    supportingContent = { 
                        Column {
                            Text("${stringResource(Res.string.stats_current)}: $current")
                            Text("${stringResource(Res.string.stats_total)}: $total")
                        }
                    },
                    trailingContent = { 
                        // Visual emoji indicator
                        Text(item.particleEmoji, fontSize = 32.sp)
                    }
                )
            }
        }
    }
}
