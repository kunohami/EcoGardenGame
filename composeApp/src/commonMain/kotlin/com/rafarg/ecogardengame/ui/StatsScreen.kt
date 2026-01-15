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

        // Total Clicks Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("👆", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Total Clicks", style = MaterialTheme.typography.labelLarge)
                    Text("${viewModel.totalClicks}", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Fruit Harvested", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Start))
        
        Spacer(modifier = Modifier.height(8.dp))

        // List of all individual fruit counters
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(viewModel.itemsList) { item ->
                val count = viewModel.fruitCounts[item.id] ?: 0
                ListItem(
                    headlineContent = { Text(item.name) },
                    supportingContent = { Text("Total collected") },
                    trailingContent = { 
                        Text("$count", style = MaterialTheme.typography.titleLarge) 
                    },
                    leadingContent = {
                        Text(item.particleEmoji, fontSize = 24.sp)
                    }
                )
            }
        }
    }
}
