package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.apple_strip
import org.jetbrains.compose.resources.painterResource

/**
 * The main game screen where the player clicks on vegetables.
 * Refactored to be agnostic of which vegetable is currently active.
 */
@Composable
fun GameScreen(viewModel: GameViewModel) {
    var menuVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- TOP COUNTER (Money Only) ---
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🪙", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${viewModel.money}",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        // --- MAIN GAME AREA ---
        // Every vegetable now handles its own rendering, movement, and particles!
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            viewModel.currentItem.Content(
                modifier = Modifier,
                onVegetableClick = { rewards -> viewModel.onVegetableClick(rewards) }
            )
        }

        // --- BOTTOM LEFT COUNTER (Specific Fruit) ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
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

        // --- FLOATING MENU ICON ---
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
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
                                    viewModel.tryUnlockItem(item)
                                    if (item.unlocked) menuVisible = false
                                }
                            }
                    ) {
                        // Using a standard size for the menu icons
                        SpriteAnimation(
                            painter = painterResource(item.resource),
                            frameCount = 3,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item.name)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (item.unlocked) "Bought" else "${item.price}")
                    }
                }
            }
        }
    }
}
