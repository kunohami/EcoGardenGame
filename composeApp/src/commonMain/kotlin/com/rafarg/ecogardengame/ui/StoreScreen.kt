package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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

/**
 * StoreScreen allows players to spend their earned currency and vegetables.
 * Players can:
 * 1. Unlock new Crops (Crops Tab)
 * 2. Purchase Global Upgrades (Upgrades Tab)
 * 3. Buy specific modifiers for each vegetable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(viewModel: GameViewModel) {
    // Keeps track of which vegetable's modifiers we are viewing (null = main store view)
    var selectedItemForUpgrades by remember { mutableStateOf<GameItem?>(null) }
    // 0 = Crops, 1 = Global Upgrades
    var selectedTab by remember { mutableStateOf(0) }
    
    val wavy = viewModel.shaderBackgroundEnabled
    val primaryText = if (wavy) Color.White else Color.Unspecified

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- STOREFRONT HEADER ---
        // We select the storefront image based on the app's current language.
        val currentLang = stringResource(Res.string.language_title)
        val isSpanish = currentLang.contains("Idioma")
        
        val storefrontResource = if (isSpanish) {
            Res.drawable.storefrontspanish_strip
        } else {
            Res.drawable.storefrontenglish_strip
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Animated Storefront
            SpriteAnimation(
                painter = painterResource(storefrontResource),
                frameCount = 3,
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )

            // Money counter overlayed top-left inside the storefront area
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpriteAnimation(
                    painter = painterResource(Res.drawable.coin_strip),
                    frameCount = 3,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${viewModel.money}",
                    style = MaterialTheme.typography.titleLarge,
                    color = primaryText,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- NAVIGATION TABS ---
        // If we are NOT viewing specific vegetable modifiers, show the two main tabs.
        if (selectedItemForUpgrades == null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tab 0: Crops (Unlockable vegetables)
                val cropsRes = if (isSpanish) Res.drawable.cropsspanish_strip else Res.drawable.cropsenglish_strip
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedTab = 0 }
                        .graphicsLayer {
                            // Subtle animation/scaling to show which tab is active
                            alpha = if (selectedTab == 0) 1f else 0.5f
                            scaleX = if (selectedTab == 0) 1.02f else 1f
                            scaleY = if (selectedTab == 0) 1.02f else 1f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    SpriteAnimation(
                        painter = painterResource(cropsRes),
                        frameCount = 3,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    )
                }

                // Tab 1: Global Upgrades (Buffs that affect everything)
                val upgradesRes = if (isSpanish) Res.drawable.upgradesspanish_strip else Res.drawable.upgradesenglish_strip
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedTab = 1 }
                        .graphicsLayer {
                            alpha = if (selectedTab == 1) 1f else 0.5f
                            scaleX = if (selectedTab == 1) 1.02f else 1f
                            scaleY = if (selectedTab == 1) 1.02f else 1f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    SpriteAnimation(
                        painter = painterResource(upgradesRes),
                        frameCount = 3,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // LazyColumn is like a RecyclerView, it only renders items visible on screen.
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (selectedTab == 0) {
                    // Show list of unlockable Crops
                    items(viewModel.itemsList) { item ->
                        UnlockCard(item, viewModel, onShowUpgrades = { selectedItemForUpgrades = it })
                    }
                } else {
                    // Show list of Global Upgrades (like weather bonuses)
                    items(viewModel.globalUpgrades) { upgrade ->
                        GlobalUpgradeCard(upgrade, viewModel)
                    }
                }
            }
        } else {
            // --- MODIFIERS VIEW ---
            // Shown when a user clicks an unlocked vegetable to see its specific upgrades.
            val item = selectedItemForUpgrades!!
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { selectedItemForUpgrades = null }) {
                    Text(stringResource(Res.string.back))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(Res.string.modifiers_title, stringResource(item.nameRes)), 
                    style = MaterialTheme.typography.titleLarge, 
                    color = primaryText
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(item.modifiers) { gpMod ->
                    ModifierCard(gpMod, viewModel)
                }
            }
        }
    }
}

/**
 * Displays a global upgrade that can be leveled up multiple times.
 */
@Composable
fun GlobalUpgradeCard(upgrade: GlobalUpgrade, viewModel: GameViewModel) {
    val nextCost = upgrade.getNextLevelCost()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpeechBubbleShape()), // Cloudy inflated shape from a custom Shape class
        colors = CardDefaults.cardColors(
            containerColor = if (upgrade.isMaxLevel) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(upgrade.nameRes), style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Level: ${upgrade.unlockedLevel} / ${upgrade.maxLevel}", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.primary
                    )
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
            
            // Show cost if not at max level
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
                    // Display costs in other vegetables
                    for (costEntry in nextCost.vegetableCosts) {
                        val vegEmoji = viewModel.itemsList.find { it.id == costEntry.key }?.particleEmoji ?: "?"
                        Text("$vegEmoji ${costEntry.value}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

/**
 * Displays a vegetable crop. Allows unlocking if locked, or clicking to see modifiers if unlocked.
 */
@Composable
fun UnlockCard(item: GameItem, viewModel: GameViewModel, onShowUpgrades: (GameItem) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpeechBubbleShape())
            .clickable(enabled = item.unlocked) { onShowUpgrades(item) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual representation of the vegetable
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
                    
                    // Show currency cost
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
                    
                    // Show vegetable requirements
                    for (costEntry in item.unlockCost.vegetableCosts) {
                        val vegEmoji = viewModel.itemsList.find { it.id == costEntry.key }?.particleEmoji ?: "?"
                        Text("$vegEmoji ${costEntry.value}", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Text(stringResource(Res.string.purchased_label), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            // Checkmark if unlocked, or Unlock button if locked
            if (item.unlocked) {
                SpriteAnimation(
                    painter = painterResource(Res.drawable.greentick_strip),
                    frameCount = 3,
                    modifier = Modifier.size(60.dp)
                )
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

/**
 * Displays a specific modifier (upgrade) for a single vegetable.
 * Modifiers can be toggled On/Off once purchased.
 */
@Composable
fun ModifierCard(gpMod: GameplayModifier, viewModel: GameViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpeechBubbleShape()),
        colors = CardDefaults.cardColors(
            containerColor = if (gpMod.isUnlocked) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(gpMod.nameRes), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(gpMod.descriptionRes), style = MaterialTheme.typography.bodySmall)
                }
                
                // If purchased, show a toggle switch. Otherwise show a buy button.
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
            
            // Show price tag if locked
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
