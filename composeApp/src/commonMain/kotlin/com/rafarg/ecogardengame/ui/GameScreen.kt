package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.rafarg.ecogardengame.model.GameItem
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.fruitmenu_strip
import ecogardengame.composeapp.generated.resources.infobunny_strip
import ecogardengame.composeapp.generated.resources.coin_strip
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
        // --- DETECT CLICKS OUTSIDE TO CLOSE MENU ---
        if (menuVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2.5f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { menuVisible = false }
            )
        }

        // --- COMPACT TOP BAR ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp) // Fixed small padding instead of statusBarsPadding
                .padding(horizontal = 16.dp)
                .zIndex(2f)
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Money Counter
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SpriteAnimation(
                        painter = painterResource(Res.drawable.coin_strip),
                        frameCount = 3,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${viewModel.money}",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )
                }

                // Specific Fruit Counter
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
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Info Bunny
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable { showTutorial = true },
                    contentAlignment = Alignment.Center
                ) {
                    SpriteAnimation(
                        painter = painterResource(Res.drawable.infobunny_strip),
                        frameCount = 3,
                        modifier = Modifier.size(44.dp)
                    )
                }

                // Fruit Selection Menu Toggle
                Box(
                    modifier = Modifier
                        .size(width = 74.dp, height = 56.dp)
                        .clip(CircleShape)
                        .clickable { menuVisible = !menuVisible },
                    contentAlignment = Alignment.Center
                ) {
                    SpriteAnimation(
                        painter = painterResource(Res.drawable.fruitmenu_strip),
                        frameCount = 3,
                        modifier = Modifier.size(width = 60.dp, height = 44.dp)
                    )
                }
            }
        }

        // --- FRUIT SELECTION MENU (Speech Bubble) ---
        if (menuVisible) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 16.dp)
                    .width(160.dp)
                    .zIndex(3f),
                shape = SpeechBubbleShape(tipAtTop = true, tipPaddingEnd = 32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.wrapContentHeight()
                ) {
                    items(viewModel.itemsList) { item ->
                        val isSelected = item.id == viewModel.currentItem.id
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                    else Color.Transparent
                                )
                                .clickable {
                                    if (item.unlocked) {
                                        viewModel.selectItem(item)
                                        menuVisible = false
                                    } else {
                                        itemToPurchase = item
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val colorFilter = if (item.unlocked) null else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                            
                            Box(modifier = Modifier.graphicsLayer { 
                                alpha = if (item.unlocked) 1f else 0.4f 
                            }) {
                                SpriteAnimation(
                                    painter = painterResource(item.resource),
                                    frameCount = 3,
                                    modifier = Modifier.size(48.dp),
                                    colorFilter = colorFilter
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- MAIN GAME AREA ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp, bottom = 0.dp) // Reduced padding to move everything up
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

        // --- CPS Meter ---
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

        // --- TUTORIAL DIALOG (Speech Bubble) ---
        if (showTutorial) {
            AlertDialog(
                onDismissRequest = { showTutorial = false },
                modifier = Modifier.clip(SpeechBubbleShape(tipAtTop = false)),
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
                        val youHave = stringResource(Res.string.you_have)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SpriteAnimation(
                                painter = painterResource(Res.drawable.coin_strip),
                                frameCount = 3,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${item.unlockCost.money} ($youHave: ${viewModel.money})",
                                color = if (hasMoney) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                        
                        item.unlockCost.vegetableCosts.forEach { (vegId, amount) ->
                            val currentCount = viewModel.fruitCounts[vegId] ?: 0
                            val vegEmoji = viewModel.itemsList.find { it.id == vegId }?.particleEmoji ?: "?"
                            val hasVeg = currentCount >= amount
                            Text("$vegEmoji $amount ($youHave: $currentCount)",
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
