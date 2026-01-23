package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.rafarg.ecogardengame.model.GameItem
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.apple_strip
import ecogardengame.composeapp.generated.resources.infobunny_strip
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The main game screen where the player clicks on vegetables.
 */
@Composable
fun GameScreen(viewModel: GameViewModel, onNavigateToStore: () -> Unit) {
    var menuVisible by remember { mutableStateOf(false) }
    var itemToPurchase by remember { mutableStateOf<GameItem?>(null) }
    var showTutorial by remember { mutableStateOf(false) }

    val textColor = if (viewModel.shaderBackgroundEnabled) Color.White else Color.Unspecified
    val secondaryTextColor = if (viewModel.shaderBackgroundEnabled) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- COMPACT TOP BAR (Status Bar Style) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .zIndex(2f)
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Money Counter (Now on the left)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${viewModel.money}",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )
                }

                // Specific Fruit Counter (Now on the right)
                val fruitCount = viewModel.fruitCounts[viewModel.currentItem.id] ?: 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(viewModel.currentItem.particleEmoji, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$fruitCount",
                        style = MaterialTheme.typography.titleMedium,
                        color = secondaryTextColor
                    )
                }
            }

            // --- TOP RIGHT ACTION BUTTONS ---
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info Bunny Tooltip Button
                SpriteAnimation(
                    painter = painterResource(Res.drawable.infobunny_strip),
                    frameCount = 3,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { showTutorial = true }
                )

                // Menu Icon (Vegetable Selector)
                SpriteAnimation(
                    painter = painterResource(Res.drawable.apple_strip),
                    frameCount = 3,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { menuVisible = !menuVisible }
                )
            }
        }

        // --- MAIN GAME AREA (Expanded to the bottom) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp, bottom = 0.dp) 
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            viewModel.currentItem.Content(
                modifier = Modifier,
                onVegetableClick = { rewards -> viewModel.onVegetableClick(rewards) },
                activeModifiers = viewModel.currentItem.modifiers,
                vibrationEnabled = viewModel.vibrationEnabled,
                vibrationIntensity = viewModel.vibrationIntensity
            )
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
                    style = MaterialTheme.typography.titleMedium,
                    color = if (viewModel.shaderBackgroundEnabled) Color.White else MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = stringResource(Res.string.cps_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (viewModel.shaderBackgroundEnabled) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                )
            }
        }

        // --- FLOATING MENU ---
        if (menuVisible) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 60.dp, end = 16.dp)
                    .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                    .padding(8.dp)
                    .zIndex(3f)
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
                                modifier = Modifier.size(32.dp),
                                colorFilter = colorFilter
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(item.nameRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (item.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // --- TUTORIAL SPEECH BUBBLE / DIALOG ---
        if (showTutorial) {
            AlertDialog(
                onDismissRequest = { showTutorial = false },
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(viewModel.currentItem.particleEmoji)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(Res.string.bunny_guide_title, stringResource(viewModel.currentItem.nameRes)))
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(viewModel.currentItem.tutorialRes))
                        
                        if (viewModel.currentItem.modifiers.isNotEmpty()) {
                            HorizontalDivider()
                            Text(stringResource(Res.string.upgrades_info), style = MaterialTheme.typography.titleSmall)
                            viewModel.currentItem.modifiers.forEach { mod ->
                                Column {
                                    Text(stringResource(mod.nameRes), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Text(stringResource(mod.descriptionRes), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTutorial = false }) {
                        Text(stringResource(Res.string.bunny_thanks))
                    }
                }
            )
        }

        // --- PURCHASE DIALOG ---
        itemToPurchase?.let { item ->
            AlertDialog(
                onDismissRequest = { itemToPurchase = null },
                title = { Text(stringResource(Res.string.unlock_item_title, stringResource(item.nameRes))) },
                text = {
                    Column {
                        Text(stringResource(Res.string.unlock_requirement))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val hasMoney = viewModel.money >= item.unlockCost.money
                        Row {
                            Text("🪙 ${item.unlockCost.money} (You have: ${viewModel.money})", 
                                color = if (hasMoney) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                        
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
                            Text(stringResource(Res.string.unlock_confirm))
                        }
                    } else {
                        Button(onClick = { 
                            onNavigateToStore()
                            itemToPurchase = null
                        }) {
                            Text(stringResource(Res.string.go_to_store))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToPurchase = null }) {
                        Text(stringResource(Res.string.close))
                    }
                }
            )
        }
    }
}
