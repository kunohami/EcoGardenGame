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

/**
 * Screen for displaying player statistics and achievements.
 */
@Composable
fun StatsScreen(viewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Statistics", style = MaterialTheme.typography.displaySmall)
        
        Spacer(modifier = Modifier.height(24.dp))

        // Total Clicks & Money Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Clicks Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("👆 Clicks", style = MaterialTheme.typography.labelLarge)
                    Text("${viewModel.totalClicks}", style = MaterialTheme.typography.headlineSmall)
                }
            }

            // Money Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🪙 Money", style = MaterialTheme.typography.labelLarge)
                    Text("Now: ${viewModel.money}", style = MaterialTheme.typography.bodyMedium)
                    Text("Total: ${viewModel.totalMoneyEarned}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Fruit Harvested", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Start))
        
        Spacer(modifier = Modifier.height(8.dp))

        // List of all individual fruit counters
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(viewModel.itemsList) { item ->
                val current = viewModel.fruitCounts[item.id] ?: 0
                val total = viewModel.totalFruitHarvested[item.id] ?: 0
                
                ListItem(
                    headlineContent = { Text(item.name) },
                    supportingContent = { 
                        Column {
                            Text("Current: $current")
                            Text("Total: $total")
                        }
                    },
                    trailingContent = { 
                        Text(item.particleEmoji, fontSize = 32.sp)
                    }
                )
            }
        }
    }
}
