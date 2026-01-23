package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafarg.ecogardengame.model.Achievement
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import ecogardengame.composeapp.generated.resources.achievements_title
import ecogardengame.composeapp.generated.resources.cancel
import ecogardengame.composeapp.generated.resources.choose_avatar
import ecogardengame.composeapp.generated.resources.close
import ecogardengame.composeapp.generated.resources.enter_name_placeholder
import ecogardengame.composeapp.generated.resources.save
import ecogardengame.composeapp.generated.resources.set_username
import ecogardengame.composeapp.generated.resources.status_locked
import ecogardengame.composeapp.generated.resources.status_unlocked
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileScreen(viewModel: GameViewModel) {
    var showNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(viewModel.username) }
    var showAvatarDialog by remember { mutableStateOf(false) }

    val currentAvatarItem = viewModel.itemsList.getOrNull(viewModel.profileImageIndex)
    
    val wavy = viewModel.shaderBackgroundEnabled
    val primaryText = if (wavy) Color.White else Color.Unspecified

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- PROFILE HEADER ---
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { showAvatarDialog = true },
            contentAlignment = Alignment.Center
        ) {
            if (currentAvatarItem != null) {
                SpriteAnimation(
                    painter = painterResource(currentAvatarItem.resource),
                    frameCount = 3,
                    modifier = Modifier
                        .size(85.dp)
                        .clip(CircleShape)
                )
            } else {
                Text("👤", fontSize = 60.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = viewModel.username,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )
            IconButton(onClick = { 
                tempName = viewModel.username
                showNameDialog = true 
            }) {
                Text("✏️", fontSize = 16.sp)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // --- ACHIEVEMENTS SECTION ---
        Text(
            stringResource(Res.string.achievements_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            color = primaryText
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(viewModel.achievements) { achievement ->
                AchievementBadge(
                    achievement = achievement,
                    isUnlocked = viewModel.unlockedAchievements.contains(achievement.id),
                    wavy = wavy
                )
            }
        }
    }

    // --- USERNAME DIALOG ---
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text(stringResource(Res.string.set_username)) },
            text = {
                TextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(Res.string.enter_name_placeholder)) }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateUsername(tempName)
                    showNameDialog = false
                }) {
                    Text(stringResource(Res.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text(stringResource(Res.string.cancel)) }
            }
        )
    }

    // --- AVATAR SELECTION DIALOG ---
    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text(stringResource(Res.string.choose_avatar)) },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(viewModel.itemsList.size) { index ->
                        val item = viewModel.itemsList[index]
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    if (viewModel.profileImageIndex == index) 
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) 
                                    else Color.Transparent
                                )
                                .border(
                                    if (viewModel.profileImageIndex == index) 2.dp else 0.dp,
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                                .clickable {
                                    viewModel.updateProfileImage(index)
                                    showAvatarDialog = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            SpriteAnimation(
                                painter = painterResource(item.resource),
                                frameCount = 3,
                                modifier = Modifier.size(60.dp).clip(CircleShape)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarDialog = false }) {
                    Text(stringResource(Res.string.close))
                }
            }
        )
    }
}

@Composable
fun AchievementBadge(achievement: Achievement, isUnlocked: Boolean, wavy: Boolean) {
    var showDetail by remember { mutableStateOf(false) }

    Surface(
        onClick = { showDetail = true },
        modifier = Modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium),
        color = if (isUnlocked) MaterialTheme.colorScheme.secondaryContainer 
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            val badgeAlpha = if (isUnlocked) 1f else 0.3f
            Text(
                text = achievement.emoji,
                fontSize = 32.sp,
                modifier = Modifier.graphicsLayer { alpha = badgeAlpha }
            )
        }
    }

    if (showDetail) {
        AlertDialog(
            onDismissRequest = { showDetail = false },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(achievement.emoji)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(achievement.nameRes))
                }
            },
            text = {
                Column {
                    Text(stringResource(achievement.descriptionRes))
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isUnlocked) {
                        Text(stringResource(Res.string.status_unlocked), color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    } else {
                        Text(stringResource(Res.string.status_locked), color = Color.Gray)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetail = false }) {
                    Text(stringResource(Res.string.close))
                }
            }
        )
    }
}
