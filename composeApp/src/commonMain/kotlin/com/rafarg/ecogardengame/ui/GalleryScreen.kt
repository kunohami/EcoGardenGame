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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun GalleryScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    val wavy = viewModel.shaderBackgroundEnabled
    val primaryText = if (wavy) Color.White else Color.Unspecified
    
    var selectedArtIndex by remember { mutableStateOf<Int?>(null) }
    var artToBuy by remember { mutableStateOf<ArtEntry?>(null) }

    val unlockedArt = ArtRepository.artEntries.filter { viewModel.isArtUnlocked(it.id) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = primaryText)
            }
            Text(
                "Art Gallery",
                style = MaterialTheme.typography.headlineMedium,
                color = primaryText,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(ArtRepository.artEntries.indices.toList()) { index ->
                val art = ArtRepository.artEntries[index]
                val isUnlocked = viewModel.isArtUnlocked(art.id)
                
                ArtThumbnail(
                    art = art,
                    isUnlocked = isUnlocked,
                    index = index,
                    textColor = primaryText, // Pass the dynamic text color
                    onClick = {
                        if (isUnlocked) {
                            selectedArtIndex = unlockedArt.indexOfFirst { it.id == art.id }
                        } else {
                            artToBuy = art
                        }
                    }
                )
            }
        }
    }

    // Purchase Dialog
    artToBuy?.let { art ->
        val currentLang = stringResource(Res.string.language_title)
        val isSpanish = currentLang.contains("Idioma")
        val artName = if (art.id in listOf("tomato", "broccoli", "bell_pepper", "garlic", "onion", "squash", "apple")) {
            stringResource(art.nameRes)
        } else {
            if (isSpanish) "Arte ${ArtRepository.artEntries.indexOf(art) + 1}" else "Art ${ArtRepository.artEntries.indexOf(art) + 1}"
        }

        AlertDialog(
            onDismissRequest = { artToBuy = null },
            title = { Text(if (isSpanish) "Desbloquear Arte" else "Unlock Art") },
            text = { Text(if (isSpanish) "¿Quieres desbloquear $artName por ${art.cost} monedas?" else "Do you want to unlock $artName for ${art.cost} coins?") },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.money >= art.cost) {
                            viewModel.unlockArt(art.id, art.cost)
                            artToBuy = null
                        }
                    },
                    enabled = viewModel.money >= art.cost
                ) {
                    Text(if (isSpanish) "Desbloquear" else "Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { artToBuy = null }) {
                    Text(if (isSpanish) "Cancelar" else "Cancel")
                }
            }
        )
    }

    // Fullscreen Viewer
    selectedArtIndex?.let { startIndex ->
        ArtViewerDialog(
            artList = unlockedArt,
            initialIndex = startIndex,
            onDismiss = { selectedArtIndex = null }
        )
    }
}

@Composable
fun ArtThumbnail(art: ArtEntry, isUnlocked: Boolean, index: Int, textColor: Color, onClick: () -> Unit) {
    // Silhouette matrix: everything black except alpha
    val silhouetteMatrix = remember {
        ColorMatrix(floatArrayOf(
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
    }

    val currentLang = stringResource(Res.string.language_title)
    val isSpanish = currentLang.contains("Idioma")
    val artName = if (art.id in listOf("tomato", "broccoli", "bell_pepper", "garlic", "onion", "squash", "apple")) {
        stringResource(art.nameRes)
    } else {
        if (isSpanish) "Arte ${index + 1}" else "Art ${index + 1}"
    }

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

@Composable
fun ArtViewerDialog(artList: List<ArtEntry>, initialIndex: Int, onDismiss: () -> Unit) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { artList.size })

    Dialog(
        onDismissRequest = onDismiss,
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
                    val currentLang = stringResource(Res.string.language_title)
                    val isSpanish = currentLang.contains("Idioma")
                    val artName = if (art.id in listOf("tomato", "broccoli", "bell_pepper", "garlic", "onion", "squash", "apple")) {
                        stringResource(art.nameRes)
                    } else {
                        if (isSpanish) "Arte ${ArtRepository.artEntries.indexOf(art) + 1}" else "Art ${ArtRepository.artEntries.indexOf(art) + 1}"
                    }

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
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Text(if (stringResource(Res.string.language_title).contains("Idioma")) "Cerrar" else "Close", color = Color.White)
                }
            }
        }
    }
}
