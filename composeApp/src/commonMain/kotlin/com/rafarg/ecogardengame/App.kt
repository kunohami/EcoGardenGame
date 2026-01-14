package com.rafarg.ecogardengame

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
 * Data class representing a single flying particle in the animation.
 *
 * @param id Unique identifier for the particle.
 * @param animatableX The animatable horizontal position of the particle.
 * @param animatableY The animatable vertical position of the particle.
 * @param animatableAlpha The animatable alpha (transparency) of the particle.
 */
data class FlyingParticle(
    val id: Long,
    val animatableX: Animatable<Float, *> = Animatable(0f),
    val animatableY: Animatable<Float, *> = Animatable(0f),
    val animatableAlpha: Animatable<Float, *> = Animatable(1f)
)

/**
 * A composable that displays a sprite animation from a given painter resource.
 *
 * @param painter The painter resource containing the sprite sheet.
 * @param frameCount The total number of frames in the sprite animation.
 * @param modifier The modifier to be applied to the animation canvas.
 * @param frameDurationMillis The duration of each frame in milliseconds.
 */
@Composable
fun SpriteAnimation(
    painter: Painter,
    frameCount: Int,
    modifier: Modifier = Modifier,
    frameDurationMillis: Long = 150
) {
    var frame by remember { mutableStateOf(0) }

    // Animate the frame index over time.
    LaunchedEffect(Unit) {
        while (true) {
            delay(frameDurationMillis)
            frame = (frame + 1) % frameCount
        }
    }

    // Draw the current frame of the sprite animation.
    Canvas(modifier = modifier) {
        val drawWidth = size.width * frameCount
        val drawHeight = size.height

        clipRect(left = 0f, top = 0f, right = size.width, bottom = size.height) {
            translate(left = -frame * size.width, top = 0f) {
                with(painter) {
                    draw(size = Size(drawWidth, drawHeight))
                }
            }
        }
    }
}

/**
 * The main application composable.
 *
 * @param prefs The DataStore instance for storing preferences.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
@Preview
fun App(prefs: DataStore<Preferences>? = null) {
    MaterialTheme {
        // --- STATE VARIABLES ---
        var clicks by remember { mutableStateOf(0) }
        var money by remember { mutableStateOf(0) }
        val scope = rememberCoroutineScope()
        val scale = remember { Animatable(1f) }
        val translationY = remember { Animatable(0f) }
        val rotation = remember { Animatable(0f) }
        var menuVisible by remember { mutableStateOf(false) }
        var currentItem by remember { mutableStateOf(items.first()) }

        var flyingParticles by remember { mutableStateOf<List<FlyingParticle>>(emptyList()) }

        // --- DATASTORE KEYS ---
        val clicksKey = intPreferencesKey("clicks")
        val moneyKey = intPreferencesKey("money")

        // --- LOAD INITIAL DATA ---
        LaunchedEffect(prefs) {
            if (prefs != null) {
                val settings = prefs.data.first()
                clicks = settings[clicksKey] ?: 0
                money = settings[moneyKey] ?: 0
            }
        }

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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Clicks: $clicks", style = MaterialTheme.typography.headlineMedium)
                    Text("Money: $money", style = MaterialTheme.typography.headlineMedium)
                }

                // --- SPRITE ANIMATION AND CLICKABLE AREA ---
                currentItem.Animate(
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
                            clicks++
                            money++

                            // Save data to DataStore
                            if (prefs != null) {
                                scope.launch {
                                    prefs.edit { settings ->
                                        settings[clicksKey] = clicks
                                        settings[moneyKey] = money
                                    }
                                }
                            }

                            // --- CLICK ANIMATIONS ---
                            currentItem.animateClick(scope, scale, translationY, rotation, clicks) {
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
                    items.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    if (item.unlocked) {
                                        currentItem = item
                                        menuVisible = false
                                    } else if (money >= item.price) {
                                        money -= item.price
                                        item.unlocked = true
                                        currentItem = item
                                        menuVisible = false
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
                Button(onClick = {
                    clicks = 0
                    money = 0
                    items.forEachIndexed { index, item ->
                        item.unlocked = index == 0
                    }
                    currentItem = items.first()
                    if (prefs != null) {
                        scope.launch {
                            prefs.edit { settings ->
                                settings[clicksKey] = clicks
                                settings[moneyKey] = money
                            }
                        }
                    }
                }) {
                    Text("Reset")
                }
            }

            // --- PARTICLE ANIMATION ---
            currentItem.ParticleEffect(flyingParticles) {
                flyingParticles = it
            }
        }
    }
}
