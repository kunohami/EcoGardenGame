package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.model.GameItem
import com.rafarg.ecogardengame.model.GameplayModifier
import com.rafarg.ecogardengame.model.GlobalUpgrade
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.coin_strip
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(viewModel: GameViewModel) {
    var selectedItemForUpgrades by remember { mutableStateOf<GameItem?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    
    val wavy = viewModel.shaderBackgroundEnabled
    val primaryText = if (wavy) Color.White else Color.Unspecified

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(Res.string.shop_title), style = MaterialTheme.typography.displaySmall, color = primaryText)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpriteAnimation(
                painter = painterResource(Res.drawable.coin_strip),
                frameCount = 3,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("${viewModel.money}", style = MaterialTheme.typography.titleLarge, color = primaryText)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedItemForUpgrades == null) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab, 
                containerColor = Color.Transparent,
                contentColor = if (wavy) Color.White else MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0, 
                    onClick = { selectedTab = 0 },
                    selectedContentColor = if (wavy) Color.White else MaterialTheme.colorScheme.primary,
                    unselectedContentColor = if (wavy) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(stringResource(Res.string.tab_vegetables), modifier = Modifier.padding(16.dp))
                }
                Tab(
                    selected = selectedTab == 1, 
                    onClick = { selectedTab = 1 },
                    selectedContentColor = if (wavy) Color.White else MaterialTheme.colorScheme.primary,
                    unselectedContentColor = if (wavy) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(stringResource(Res.string.tab_upgrades), modifier = Modifier.padding(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp), // Increased spacing for the puffy shape
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (selectedTab == 0) {
                    items(viewModel.itemsList) { item ->
                        UnlockCard(item, viewModel, onShowUpgrades = { selectedItemForUpgrades = it })
                    }
                } else {
                    items(viewModel.globalUpgrades) { upgrade ->
                        GlobalUpgradeCard(upgrade, viewModel)
                    }
                }
            }
        } else {
            // Modifiers View for a specific item
            val item = selectedItemForUpgrades!!
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { selectedItemForUpgrades = null }) {
                    Text(stringResource(Res.string.back))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(stringResource(Res.string.modifiers_title, stringResource(item.nameRes)), style = MaterialTheme.typography.titleLarge, color = primaryText)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(item.modifiers) { gpMod ->
                    ModifierCard(gpMod, viewModel)
                }
            }
        }
    }
}

@Composable
fun GlobalUpgradeCard(upgrade: GlobalUpgrade, viewModel: GameViewModel) {
    val nextCost = upgrade.getNextLevelCost()
    val wavy = viewModel.shaderBackgroundEnabled
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpeechBubbleShape()), // Cloudy inflated shape
        colors = CardDefaults.cardColors(
            containerColor = if (wavy) Color.Black.copy(alpha = 0.4f) 
                            else if (upgrade.isMaxLevel) MaterialTheme.colorScheme.secondaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) { // Padding increased for puffy shape
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(upgrade.nameRes), style = MaterialTheme.typography.titleMedium)
                    Text("Level: ${upgrade.unlockedLevel} / ${upgrade.maxLevel}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(upgrade.descriptionRes), style = MaterialTheme.typography.bodySmall)
                }
                
                if (upgrade.isMaxLevel) {
                    Text(stringResource(Res.string.max_level), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                } else {
                    val canAfford = viewModel.canAfford(nextCost)
                    Button(
                        onClick = { viewModel.tryUnlockGlobalUpgrade(upgrade) },
                        enabled = canAfford
                    ) {
                        Text(if (upgrade.unlockedLevel == 0) stringResource(Res.string.buy) else stringResource(Res.string.upgrade))
                    }
                }
            }
            
            if (!upgrade.isMaxLevel) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(Res.string.upgrade_cost_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (nextCost.money > 0) {
                        SpriteAnimation(
                            painter = painterResource(Res.drawable.coin_strip),
                            frameCount = 3,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${nextCost.money}", style = MaterialTheme.typography.labelSmall)
                    }
                    for (costEntry in nextCost.vegetableCosts) {
                        val vegEmoji = viewModel.itemsList.find { it.id == costEntry.key }?.particleEmoji ?: "?"
                        Text("$vegEmoji ${costEntry.value}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun UnlockCard(item: GameItem, viewModel: GameViewModel, onShowUpgrades: (GameItem) -> Unit) {
    val wavy = viewModel.shaderBackgroundEnabled
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpeechBubbleShape()) // Cloudy inflated shape
            .clickable(enabled = item.unlocked) { onShowUpgrades(item) },
        colors = CardDefaults.cardColors(
            containerColor = if (wavy) Color.Black.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp), // Padding increased for puffy shape
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
                Text(stringResource(item.nameRes), style = MaterialTheme.typography.titleMedium)
                
                if (!item.unlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(stringResource(Res.string.cost_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    
                    if (item.unlockCost.money > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SpriteAnimation(
                                painter = painterResource(Res.drawable.coin_strip),
                                frameCount = 3,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${item.unlockCost.money}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    
                    for (costEntry in item.unlockCost.vegetableCosts) {
                        val vegEmoji = viewModel.itemsList.find { it.id == costEntry.key }?.particleEmoji ?: "?"
                        Text("$vegEmoji ${costEntry.value}", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Text(stringResource(Res.string.purchased_label), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
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
                    Text(stringResource(Res.string.unlock_button))
                }
            }
        }
    }
}

@Composable
fun ModifierCard(gpMod: GameplayModifier, viewModel: GameViewModel) {
    val wavy = viewModel.shaderBackgroundEnabled
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpeechBubbleShape()), // Cloudy inflated shape
        colors = CardDefaults.cardColors(
            containerColor = if (wavy) Color.Black.copy(alpha = 0.4f) 
                            else if (gpMod.isUnlocked) MaterialTheme.colorScheme.secondaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) { // Padding increased for puffy shape
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(gpMod.nameRes), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(gpMod.descriptionRes), style = MaterialTheme.typography.bodySmall)
                }
                
                if (gpMod.isUnlocked) {
                    Switch(
                        checked = gpMod.isEnabled,
                        onCheckedChange = { viewModel.toggleModifier(gpMod) }
                    )
                } else {
                    val canAfford = viewModel.canAfford(gpMod.unlockCost)
                    Button(
                        onClick = { viewModel.tryUnlockModifier(gpMod) },
                        enabled = canAfford
                    ) {
                        Text(stringResource(Res.string.buy))
                    }
                }
            }
            
            if (!gpMod.isUnlocked) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (gpMod.unlockCost.money > 0) {
                        SpriteAnimation(
                            painter = painterResource(Res.drawable.coin_strip),
                            frameCount = 3,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${gpMod.unlockCost.money}", style = MaterialTheme.typography.labelSmall)
                    }
                    for (costEntry in gpMod.unlockCost.vegetableCosts) {
                        val vegEmoji = viewModel.itemsList.find { it.id == costEntry.key }?.particleEmoji ?: "?"
                        Text("$vegEmoji ${costEntry.value}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
