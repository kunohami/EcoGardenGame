package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.model.GameItem
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import org.jetbrains.compose.resources.painterResource

/**
 * Screen for purchasing upgrades and new items using multi-currency costs.
 */
@Composable
fun StoreScreen(viewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Garden Shop", style = MaterialTheme.typography.displaySmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Current balance display
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("🪙 ${viewModel.money}", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Now showing all items, including unlocked ones
            items(viewModel.itemsList) { item ->
                UnlockCard(item, viewModel)
            }
        }
    }
}

@Composable
fun UnlockCard(item: GameItem, viewModel: GameViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Use the animated bitmap for the item icon in the store
            Box(modifier = Modifier.size(60.dp)) {
                SpriteAnimation(
                    painter = painterResource(item.resource),
                    frameCount = 3,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                
                // Show Costs in a Column to allow multiple lines
                if (!item.unlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Unlock Cost:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    
                    if (item.unlockCost.money > 0) {
                        Text("🪙 ${item.unlockCost.money}", style = MaterialTheme.typography.bodySmall)
                    }
                    
                    item.unlockCost.vegetableCosts.forEach { (vegId, amount) ->
                        val vegEmoji = viewModel.itemsList.find { it.id == vegId }?.particleEmoji ?: "?"
                        Text("$vegEmoji $amount", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Text("Purchased", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            
            if (item.unlocked) {
                // Show purchased checkmark
                Text("✅", fontSize = 32.sp)
            } else {
                val canAfford = viewModel.canAfford(item)
                Button(
                    onClick = { viewModel.tryUnlockItem(item) },
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                ) {
                    Text("Unlock")
                }
            }
        }
    }
}
