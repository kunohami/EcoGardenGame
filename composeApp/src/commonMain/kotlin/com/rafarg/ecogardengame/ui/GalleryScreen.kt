package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rafarg.ecogardengame.data.ArtRepository
import com.rafarg.ecogardengame.model.ArtEntry
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * GalleryScreen displays a grid of unlockable art pieces.
 * Locked items appear as silhouettes, and unlocked items can be viewed in a fullscreen slider.
 */
@Composable
fun GalleryScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    val wavy = viewModel.shaderBackgroundEnabled
    val primaryText = if (wavy) Color.White else Color.Unspecified
    
    // State to track which art is being viewed in fullscreen
    var selectedArtIndex by remember { mutableStateOf<Int?>(null) }
    // State to track which art is currently being prompted for purchase
    var artToBuy by remember { mutableStateOf<ArtEntry?>(null) }

    // List of art pieces the player has already purchased
    val unlockedArt = ArtRepository.artEntries.filter { viewModel.isArtUnlocked(it.id) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back_label), tint = primaryText)
            }
            Text(
                text = stringResource(Res.string.achievements_title), // Title label
                style = MaterialTheme.typography.headlineMedium,
                color = primaryText,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- GRID OF ART ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Iterate over all available art entries in the repository
            items(ArtRepository.artEntries.indices.toList()) { index ->
                val art = ArtRepository.artEntries[index]
                val isUnlocked = viewModel.isArtUnlocked(art.id)
                
                ArtThumbnail(
                    art = art,
                    isUnlocked = isUnlocked,
                    index = index,
                    textColor = primaryText,
                    onClick = {
                        if (isUnlocked) {
                            // Find the index within the "unlockedArt" list for the pager
                            selectedArtIndex = unlockedArt.indexOfFirst { it.id == art.id }
                        } else {
                            // Show purchase dialog
                            artToBuy = art
                        }
                    }
                )
            }
        }
    }

    // --- PURCHASE DIALOG ---
    artToBuy?.let { art ->
        val artName = getArtDisplayName(art, ArtRepository.artEntries.indexOf(art))

        AlertDialog(
            onDismissRequest = { artToBuy = null },
            title = { Text(stringResource(Res.string.unlock_art_title)) },
            text = { Text(stringResource(Res.string.unlock_art_msg, artName, art.cost)) },
            confirmButton = {
                Button(
                    onClick = {
                        // Apply potential store discounts from upgrades
                        if (viewModel.money >= viewModel.calculateDiscountedPrice(art.cost)) {
                            viewModel.unlockArt(art.id, art.cost)
                            artToBuy = null
                        }
                    },
                    enabled = viewModel.money >= viewModel.calculateDiscountedPrice(art.cost)
                ) {
                    Text(stringResource(Res.string.unlock_label))
                }
            },
            dismissButton = {
                TextButton(onClick = { artToBuy = null }) {
                    Text(stringResource(Res.string.cancel_label))
                }
            }
        )
    }

    // --- FULLSCREEN VIEWER ---
    // If an index is selected, show a Dialog that covers the screen with a swipable pager.
    selectedArtIndex?.let { startIndex ->
        ArtViewerDialog(
            artList = unlockedArt,
            initialIndex = startIndex,
            onDismiss = { selectedArtIndex = null }
        )
    }
}

/**
 * Helper function to get the localized name of an art piece.
 */
@Composable
fun getArtDisplayName(art: ArtEntry, index: Int): String {
    return if (art.id in listOf("tomato", "broccoli", "bell_pepper", "garlic", "onion", "squash", "apple")) {
        stringResource(art.nameRes)
    } else {
        stringResource(Res.string.art_name_label, index + 1)
    }
}

/**
 * A thumbnail component that shows either the art or a silhouette if locked.
 */
@Composable
fun ArtThumbnail(art: ArtEntry, isUnlocked: Boolean, index: Int, textColor: Color, onClick: () -> Unit) {
    // Silhouette matrix: turns all pixels to black while keeping transparency
    val silhouetteMatrix = remember {
        ColorMatrix(floatArrayOf(
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
    }

    val artName = getArtDisplayName(art, index)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            SpriteAnimation(
                painter = painterResource(art.resource),
                frameCount = art.frameCount,
                modifier = Modifier.fillMaxSize(0.8f),
                colorFilter = if (isUnlocked) null else ColorFilter.colorMatrix(silhouetteMatrix)
            )
            if (!isUnlocked) {
                Text("🔒", modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp), fontSize = 12.sp)
            }
        }
        Text(artName, fontSize = 10.sp, maxLines = 1, color = textColor)
    }
}

/**
 * A full-screen dialog using HorizontalPager to swipe through unlocked art.
 */
@Composable
fun ArtViewerDialog(artList: List<ArtEntry>, initialIndex: Int, onDismiss: () -> Unit) {
    // HorizontalPager state keeps track of the current page
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { artList.size })

    Dialog(
        onDismissRequest = onDismiss,
        // usePlatformDefaultWidth = false allows the dialog to be truly full-screen
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.9f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val art = artList[page]
                    val artName = getArtDisplayName(art, ArtRepository.artEntries.indexOf(art))

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SpriteAnimation(
                            painter = painterResource(art.resource),
                            frameCount = art.frameCount,
                            modifier = Modifier.fillMaxWidth(0.8f).aspectRatio(1f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            artName,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
                
                // Exit button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Text(stringResource(Res.string.close_label), color = Color.White)
                }
            }
        }
    }
}
