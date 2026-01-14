package com.rafarg.ecogardengame.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import com.rafarg.ecogardengame.model.BellPepper
import com.rafarg.ecogardengame.model.Broccoli
import com.rafarg.ecogardengame.model.FlyingParticle
import com.rafarg.ecogardengame.model.GameItem
import com.rafarg.ecogardengame.model.Garlic
import com.rafarg.ecogardengame.model.PurpleOnion
import com.rafarg.ecogardengame.model.Squash
import com.rafarg.ecogardengame.model.Tomato
import org.jetbrains.compose.ui.tooling.preview.Preview

// --- RESOURCES IMPORT ---
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.apple_strip
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

val items: List<GameItem> = listOf(
    Tomato(),
    Broccoli(),
    BellPepper(),
    Garlic(),
    PurpleOnion(),
    Squash()
)

/**
 * The main application composable.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun App(prefs: DataStore<Preferences>? = null) {
    val viewModel: GameViewModel = viewModel { GameViewModel(prefs) }
    
    MaterialTheme {
        val scope = rememberCoroutineScope()
        val scale = remember { Animatable(1f) }
        val translationY = remember { Animatable(0f) }
        val rotation = remember { Animatable(0f) }
        var menuVisible by remember { mutableStateOf(false) }

        var flyingParticles by remember { mutableStateOf<List<FlyingParticle>>(emptyList()) }

        // --- UI LAYOUT ---
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- SCORE DISPLAY ---
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Clicks: ${viewModel.totalClicks}", style = MaterialTheme.typography.titleMedium)
                        Text("Money: $${viewModel.money}", style = MaterialTheme.typography.titleLarge)
                    }
                    
                    // Specific Fruit Counter (Only shows for the current item)
                    val fruitCount = viewModel.fruitCounts[viewModel.currentItem.id] ?: 0
                    Text(
                        text = "${viewModel.currentItem.name}s: $fruitCount",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // --- SPRITE ANIMATION AND CLICKABLE AREA ---
                viewModel.currentItem.Animate(
                    modifier = Modifier
                        .size(200.dp)
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                            this.translationY = translationY.value
                            rotationZ = rotation.value
                        }
                        .clickable (
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null) {

                            // --- CLICK HANDLER ---
                            viewModel.onVegetableClick()

                            // --- CLICK ANIMATIONS ---
                            viewModel.currentItem.animateClick(
                                scope, 
                                scale, 
                                translationY, 
                                rotation, 
                                viewModel.totalClicks
                            ) {
                                flyingParticles = it
                            }
                        }
                )
            }

            // --- MENU ICON ---
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
                            item.Animate(modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(item.name)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (item.unlocked) "Bought" else "${item.price}")
                        }
                    }
                }
            }

            // --- RESET BUTTON ---
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                Button(onClick = { viewModel.resetGame() }) {
                    Text("Reset")
                }
            }

            // --- PARTICLE ANIMATION ---
            viewModel.currentItem.ParticleEffect(flyingParticles) {
                flyingParticles = it
            }
        }
    }
}
