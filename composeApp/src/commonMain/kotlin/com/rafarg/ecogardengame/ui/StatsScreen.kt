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

@Composable
fun StatsScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onBack) {
                Text("Back")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Statistics", style = MaterialTheme.typography.displaySmall)
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("👆 Clicks", style = MaterialTheme.typography.labelLarge)
                    Text("${viewModel.totalClicks}", style = MaterialTheme.typography.headlineSmall)
                }
            }

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
