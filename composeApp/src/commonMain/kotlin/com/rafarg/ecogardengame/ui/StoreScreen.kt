package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.model.GameItem
import com.rafarg.ecogardengame.model.GameplayModifier
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import org.jetbrains.compose.resources.painterResource

/**
 * Screen for purchasing upgrades and new items using multi-currency costs.
 */
@Composable
fun StoreScreen(viewModel: GameViewModel) {
    var selectedItemForUpgrades by remember { mutableStateOf<GameItem?>(null) }

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

        if (selectedItemForUpgrades == null) {
            Text("Vegetables", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.itemsList) { item ->
                    UnlockCard(item, viewModel, onShowUpgrades = { selectedItemForUpgrades = it })
                }
            }
        } else {
            // Upgrades View for a specific item
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { selectedItemForUpgrades = null }) {
                    Text("Back")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Upgrades: ${selectedItemForUpgrades?.name}", style = MaterialTheme.typography.titleLarge)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(selectedItemForUpgrades?.modifiers ?: emptyList()) { modifier ->
                    ModifierCard(modifier, viewModel)
                }
            }
        }
    }
}

@Composable
fun UnlockCard(item: GameItem, viewModel: GameViewModel, onShowUpgrades: (GameItem) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = item.unlocked) { onShowUpgrades(item) },
        colors = CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    Text("Purchased - Tap for Upgrades", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            if (item.unlocked) {
                Text("✅", fontSize = 32.sp)
            } else {
                val canAfford = viewModel.canAfford(item.unlockCost)
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

@Composable
fun ModifierCard(modifier: GameplayModifier, viewModel: GameViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (modifier.isUnlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(modifier.name, style = MaterialTheme.typography.titleMedium)
                    Text(modifier.description, style = MaterialTheme.typography.bodySmall)
                }
                
                if (modifier.isUnlocked) {
                    Switch(
                        checked = modifier.isEnabled,
                        onCheckedChange = { viewModel.toggleModifier(modifier) }
                    )
                } else {
                    val canAfford = viewModel.canAfford(modifier.unlockCost)
                    Button(
                        onClick = { viewModel.tryUnlockModifier(modifier) },
                        enabled = canAfford
                    ) {
                        Text("Buy")
                    }
                }
            }
            
            if (!modifier.isUnlocked) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (modifier.unlockCost.money > 0) {
                        Text("🪙 ${modifier.unlockCost.money}", style = MaterialTheme.typography.labelSmall)
                    }
                    modifier.unlockCost.vegetableCosts.forEach { (vegId, amount) ->
                        val vegEmoji = viewModel.itemsList.find { it.id == vegId }?.particleEmoji ?: "?"
                        Text("$vegEmoji $amount", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
