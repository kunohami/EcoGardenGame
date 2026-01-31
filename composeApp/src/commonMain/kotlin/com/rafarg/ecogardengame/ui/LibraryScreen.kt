package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.data.ArtRepository
import com.rafarg.ecogardengame.model.ArtEntry
import com.rafarg.ecogardengame.model.LibraryCategory
import com.rafarg.ecogardengame.model.LibraryEntry
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.coin_strip
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(viewModel: GameViewModel) {
    var selectedCategory by remember { mutableStateOf<LibraryCategory?>(null) }
    var entryToShowInDialog by remember { mutableStateOf<LibraryEntry?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Knowledge, 1: Gallery
    
    // Gallery state
    var selectedArtIndex by remember { mutableStateOf<Int?>(null) }
    var artToBuy by remember { mutableStateOf<ArtEntry?>(null) }
    val unlockedArt = ArtRepository.artEntries.filter { viewModel.isArtUnlocked(it.id) }

    val wavy = viewModel.shaderBackgroundEnabled
    val primaryText = if (wavy) Color.White else Color.Unspecified
    val secondaryText = if (wavy) Color.White.copy(alpha = 0.7f) else Color.Gray

    val currentLang = stringResource(Res.string.language_title)
    val isSpanish = currentLang.contains("Idioma")

    // Handle back button when in a category or in gallery fullscreen
    BackHandler(enabled = selectedCategory != null || selectedArtIndex != null) {
        if (selectedArtIndex != null) {
            selectedArtIndex = null
        } else {
            selectedCategory = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val currentCategory = selectedCategory
        if (currentCategory == null) {
            // Header with Library Front
            val libraryFrontResource = if (isSpanish) {
                Res.drawable.libraryfrontspanish_strip
            } else {
                Res.drawable.libraryfrontenglish_strip
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SpriteAnimation(
                    painter = painterResource(libraryFrontResource),
                    frameCount = 3,
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Tab Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Knowledge Tab
                val knowledgeRes = if (isSpanish) Res.drawable.knowledgespanish_strip else Res.drawable.knowledgeenglish_strip
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedTab = 0 }
                        .graphicsLayer {
                            alpha = if (selectedTab == 0) 1f else 0.5f
                            scaleX = if (selectedTab == 0) 1.02f else 1f
                            scaleY = if (selectedTab == 0) 1.02f else 1f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    SpriteAnimation(
                        painter = painterResource(knowledgeRes),
                        frameCount = 3,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    )
                }

                // Gallery Tab
                val galleryRes = if (isSpanish) Res.drawable.galleryspanish_strip else Res.drawable.galleryenglish_strip
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
                        painter = painterResource(galleryRes),
                        frameCount = 3,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (selectedTab == 0) {
                // Knowledge Content
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(viewModel.libraryCategories) { category ->
                        CategoryCard(category, secondaryText, wavy) {
                            selectedCategory = it
                        }
                    }
                }
            } else {
                // Gallery Content
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth().weight(1f),
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
                            textColor = primaryText,
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
        } else {
            // Category Detail View
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { selectedCategory = null }) {
                    Text(stringResource(Res.string.back))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(stringResource(currentCategory.nameRes), style = MaterialTheme.typography.titleLarge, color = primaryText)
                
                Spacer(modifier = Modifier.weight(1f))

                // Money counter overlayed top-right in facts view
                Row(
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(currentCategory.entries) { entry ->
                    EntryListItem(entry, viewModel, wavy) {
                        entryToShowInDialog = it
                    }
                }
            }
        }
    }

    // Knowledge Dialog
    entryToShowInDialog?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToShowInDialog = null },
            modifier = Modifier.clip(SpeechBubbleShape()),
            title = { Text(stringResource(entry.titleRes)) },
            text = { Text(stringResource(entry.contentRes)) },
            confirmButton = {
                TextButton(onClick = { entryToShowInDialog = null }) {
                    Text(stringResource(Res.string.got_it))
                }
            }
        )
    }

    // Gallery Purchase Dialog
    artToBuy?.let { art ->
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

    // Gallery Fullscreen Viewer
    selectedArtIndex?.let { startIndex ->
        ArtViewerDialog(
            artList = unlockedArt,
            initialIndex = startIndex,
            onDismiss = { selectedArtIndex = null }
        )
    }
}

@Composable
fun CategoryCard(category: LibraryCategory, labelColor: Color, wavy: Boolean, onClick: (LibraryCategory) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpeechBubbleShape())
            .clickable { onClick(category) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(category.icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(stringResource(category.nameRes), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
            val unlockedCount = category.entries.count { it.isUnlocked }
            Text("$unlockedCount / 10", style = MaterialTheme.typography.labelSmall, color = labelColor)
        }
    }
}

@Composable
fun EntryListItem(entry: LibraryEntry, viewModel: GameViewModel, wavy: Boolean, onShowContent: (LibraryEntry) -> Unit) {
    val lockColor = if (wavy) Color.White.copy(alpha = 0.5f) else Color.Gray
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpeechBubbleShape())
            .clickable(enabled = entry.isUnlocked) { onShowContent(entry) },
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isUnlocked) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(entry.titleRes),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.isUnlocked) MaterialTheme.colorScheme.onSecondaryContainer else lockColor
                )
                if (!entry.isUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (entry.cost.money > 0) {
                            SpriteAnimation(
                                painter = painterResource(Res.drawable.coin_strip),
                                frameCount = 3,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${entry.cost.money}", style = MaterialTheme.typography.labelSmall, color = lockColor)
                        }
                        entry.cost.vegetableCosts.forEach { cost ->
                            val emoji = viewModel.itemsList.find { it.id == cost.key }?.particleEmoji ?: ""
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$emoji ${cost.value}", style = MaterialTheme.typography.labelSmall, color = lockColor)
                        }
                    }
                }
            }
            
            if (entry.isUnlocked) {
                SpriteAnimation(
                    painter = painterResource(Res.drawable.greentick_strip),
                    frameCount = 3,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                val canAfford = viewModel.canAfford(entry.cost)
                Button(
                    onClick = { viewModel.tryUnlockLibraryEntry(entry) },
                    enabled = canAfford,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(stringResource(Res.string.unlock_button), fontSize = 12.sp)
                }
            }
        }
    }
}
