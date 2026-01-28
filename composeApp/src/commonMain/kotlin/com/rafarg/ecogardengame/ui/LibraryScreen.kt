package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.model.LibraryCategory
import com.rafarg.ecogardengame.model.LibraryEntry
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.coin_strip
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LibraryScreen(viewModel: GameViewModel) {
    var selectedCategory by remember { mutableStateOf<LibraryCategory?>(null) }
    var entryToShowInDialog by remember { mutableStateOf<LibraryEntry?>(null) }

    // Use a local copy to ensure safety during recomposition when switching back
    val currentCategory = selectedCategory
    
    val wavy = viewModel.shaderBackgroundEnabled
    val primaryText = if (wavy) Color.White else Color.Unspecified
    val secondaryText = if (wavy) Color.White.copy(alpha = 0.7f) else Color.Gray

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentCategory == null) {
            Text(stringResource(Res.string.library_title), style = MaterialTheme.typography.displaySmall, color = primaryText)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpriteAnimation(
                    painter = painterResource(Res.drawable.coin_strip),
                    frameCount = 3,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${viewModel.money}", style = MaterialTheme.typography.titleLarge, color = primaryText)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(viewModel.libraryCategories) { category ->
                    CategoryCard(category, secondaryText, wavy) {
                        selectedCategory = it
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
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(currentCategory.entries) { entry ->
                    EntryListItem(entry, viewModel, wavy) {
                        entryToShowInDialog = it
                    }
                }
            }
        }
    }

    // Info Dialog
    entryToShowInDialog?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToShowInDialog = null },
            modifier = Modifier.clip(SpeechBubbleShape()), // Cloudy shape for the dialog too
            title = { Text(stringResource(entry.titleRes)) },
            text = { Text(stringResource(entry.contentRes)) },
            confirmButton = {
                TextButton(onClick = { entryToShowInDialog = null }) {
                    Text(stringResource(Res.string.got_it))
                }
            }
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
                Text("📖", fontSize = 24.sp)
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
