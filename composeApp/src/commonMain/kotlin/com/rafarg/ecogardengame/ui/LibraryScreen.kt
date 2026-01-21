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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.model.LibraryCategory
import com.rafarg.ecogardengame.model.LibraryEntry
import com.rafarg.ecogardengame.viewmodel.GameViewModel

@Composable
fun LibraryScreen(viewModel: GameViewModel) {
    var selectedCategory by remember { mutableStateOf<LibraryCategory?>(null) }
    var entryToShowInDialog by remember { mutableStateOf<LibraryEntry?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (selectedCategory == null) {
            Text("Knowledge Library", style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("🪙 ${viewModel.money}", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.libraryCategories) { category ->
                    CategoryCard(category) {
                        selectedCategory = it
                    }
                }
            }
        } else {
            // Category Detail View
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { selectedCategory = null }) {
                    Text("Back")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(selectedCategory!!.name, style = MaterialTheme.typography.titleLarge)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedCategory!!.entries) { entry ->
                    EntryListItem(entry, viewModel) {
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
            title = { Text(entry.title) },
            text = { Text(entry.content) },
            confirmButton = {
                TextButton(onClick = { entryToShowInDialog = null }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
fun CategoryCard(category: LibraryCategory, onClick: (LibraryCategory) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(category) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(category.icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(category.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
            val unlockedCount = category.entries.count { it.isUnlocked }
            Text("$unlockedCount / 10", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun EntryListItem(entry: LibraryEntry, viewModel: GameViewModel, onShowContent: (LibraryEntry) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = entry.isUnlocked) { onShowContent(entry) },
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isUnlocked) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.isUnlocked) MaterialTheme.colorScheme.onSecondaryContainer else Color.Gray
                )
                if (!entry.isUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (entry.cost.money > 0) {
                            Text("🪙 ${entry.cost.money}", style = MaterialTheme.typography.labelSmall)
                        }
                        entry.cost.vegetableCosts.forEach { (id, amount) ->
                            val emoji = items.find { it.id == id }?.particleEmoji ?: ""
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$emoji $amount", style = MaterialTheme.typography.labelSmall)
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
                    Text("Unlock", fontSize = 12.sp)
                }
            }
        }
    }
}
