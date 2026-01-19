package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.rafarg.ecogardengame.model.GameItem
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.apple_strip
import org.jetbrains.compose.resources.painterResource

/**
 * The main game screen where the player clicks on vegetables.
 */
@Composable
fun GameScreen(viewModel: GameViewModel, onNavigateToStore: () -> Unit) {
    var menuVisible by remember { mutableStateOf(false) }
    var itemToPurchase by remember { mutableStateOf<GameItem?>(null) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- TOP UI BAR (Z-Index Protection) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp)
                .zIndex(1f) // Ensure it's above the game area
        ) {
            Row(
                modifier = Modifier.align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🪙", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${viewModel.money}",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        // --- MAIN GAME AREA (Safe Zone Restricted) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp, bottom = 100.dp) // Define safe zone margins
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            viewModel.currentItem.Content(
                modifier = Modifier,
                onVegetableClick = { rewards -> viewModel.onVegetableClick(rewards) },
                activeModifiers = viewModel.currentItem.modifiers
            )
        }

        // --- BOTTOM LEFT COUNTER (Specific Fruit) ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .zIndex(1f)
        ) {
            val fruitCount = viewModel.fruitCounts[viewModel.currentItem.id] ?: 0
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(viewModel.currentItem.particleEmoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$fruitCount",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // --- BOTTOM RIGHT COUNTER (CPS Meter) ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .zIndex(1f)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${viewModel.currentCps.toInt()}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "CPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                )
            }
        }

        // --- FLOATING MENU ICON ---
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).zIndex(2f)) {
            SpriteAnimation(
                painter = painterResource(Res.drawable.apple_strip),
                frameCount = 3,
                modifier = Modifier
                    .size(60.dp)
                    .clickable { menuVisible = !menuVisible }
            )
        }

        // --- FLOATING MENU ---
        if (menuVisible) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 16.dp)
                    .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                    .padding(8.dp)
                    .zIndex(2f)
            ) {
                viewModel.itemsList.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                if (item.unlocked) {
                                    viewModel.selectItem(item)
                                    menuVisible = false
                                } else {
                                    // Show purchase dialog for locked items
                                    itemToPurchase = item
                                }
                            }
                    ) {
                        val colorFilter = if (item.unlocked) null else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                        
                        Box(modifier = Modifier.graphicsLayer { 
                            alpha = if (item.unlocked) 1f else 0.5f 
                        }) {
                            SpriteAnimation(
                                painter = painterResource(item.resource),
                                frameCount = 3,
                                modifier = Modifier.size(40.dp),
                                colorFilter = colorFilter
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.name,
                            color = if (item.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // --- PURCHASE DIALOG ---
        itemToPurchase?.let { item ->
            AlertDialog(
                onDismissRequest = { itemToPurchase = null },
                title = { Text("Unlock ${item.name}") },
                text = {
                    Column {
                        Text("You need the following to unlock this item:")
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Show Money Cost
                        val hasMoney = viewModel.money >= item.unlockCost.money
                        Row {
                            Text("🪙 ${item.unlockCost.money} (You have: ${viewModel.money})", 
                                color = if (hasMoney) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                        
                        // Show Vegetable Costs
                        item.unlockCost.vegetableCosts.forEach { (vegId, amount) ->
                            val currentCount = viewModel.fruitCounts[vegId] ?: 0
                            val vegEmoji = viewModel.itemsList.find { it.id == vegId }?.particleEmoji ?: "?"
                            val hasVeg = currentCount >= amount
                            Text("$vegEmoji $amount (You have: $currentCount)", 
                                color = if (hasVeg) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    if (viewModel.canAfford(item.unlockCost)) {
                        Button(onClick = {
                            viewModel.tryUnlockItem(item)
                            itemToPurchase = null
                            menuVisible = false
                        }) {
                            Text("Unlock Now")
                        }
                    } else {
                        Button(onClick = { 
                            onNavigateToStore()
                            itemToPurchase = null
                        }) {
                            Text("Go to Store")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToPurchase = null }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
