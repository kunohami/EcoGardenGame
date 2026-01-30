package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import com.rafarg.ecogardengame.data.ArtRepository
import com.rafarg.ecogardengame.model.Achievement
import com.rafarg.ecogardengame.viewmodel.GameViewModel
import com.rafarg.ecogardengame.viewmodel.PublicProfile
import ecogardengame.composeapp.generated.resources.*
import ecogardengame.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: GameViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    val wavy = viewModel.shaderBackgroundEnabled
    val primaryText = if (wavy) Color.White else Color.Unspecified

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = if (wavy) Color.Black.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                contentColor = if (wavy) Color.White else MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(Res.string.tab_my_profile)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(Res.string.tab_search_players)) }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> MyProfileTab(viewModel, primaryText)
                1 -> SearchPlayersTab(viewModel, primaryText)
            }
        }
    }
}

@Composable
fun MyProfileTab(
    viewModel: GameViewModel,
    textColor: Color
) {
    var showNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(viewModel.username) }
    var showAvatarDialog by remember { mutableStateOf(false) }

    // Find the current avatar in items or gallery art
    val currentAvatarResource = viewModel.itemsList.find { it.id == viewModel.profileImageId }?.resource 
        ?: ArtRepository.artEntries.find { it.id == viewModel.profileImageId }?.resource
        ?: Res.drawable.tomato_strip // Fallback

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- PROFILE HEADER ---
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { showAvatarDialog = true },
            contentAlignment = Alignment.Center
        ) {
            SpriteAnimation(
                painter = painterResource(currentAvatarResource),
                frameCount = 3,
                modifier = Modifier.size(70.dp).clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = viewModel.username,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            IconButton(onClick = {
                tempName = viewModel.username
                showNameDialog = true
            }) {
                Text("✏️", fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            stringResource(Res.string.sync_notice),
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.6f)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            stringResource(Res.string.achievements_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            color = textColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(viewModel.achievements) { achievement ->
                AchievementBadge(
                    achievement = achievement,
                    isUnlocked = viewModel.unlockedAchievements.contains(achievement.id)
                )
            }
        }
    }

    // --- DIALOGS ---
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

    if (showAvatarDialog) {
        // Merge vegetables and purchased art
        val availableAvatars = (viewModel.itemsList.map { it.id to it.resource } + 
                              ArtRepository.artEntries.filter { viewModel.isArtUnlocked(it.id) }.map { it.id to it.resource })
                              .distinctBy { it.first }

        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text(stringResource(Res.string.choose_avatar)) },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(availableAvatars) { (id, resource) ->
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(if (viewModel.profileImageId == id) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent)
                                .border(if (viewModel.profileImageId == id) 2.dp else 0.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable {
                                    viewModel.updateProfileImage(id)
                                    showAvatarDialog = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            SpriteAnimation(
                                painter = painterResource(resource),
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
fun SearchPlayersTab(viewModel: GameViewModel, textColor: Color) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProfile by remember { mutableStateOf<PublicProfile?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(stringResource(Res.string.search_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { viewModel.searchPlayers(searchQuery) }) {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = textColor.copy(alpha = 0.7f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isSearching) {
            CircularProgressIndicator()
        } else if (viewModel.searchResults.isEmpty() && searchQuery.isNotEmpty()) {
            Text(stringResource(Res.string.no_players_found), color = textColor.copy(alpha = 0.6f))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.searchResults) { profile ->
                    PublicProfileCard(profile, viewModel) {
                        selectedProfile = it
                    }
                }
            }
        }
    }

    selectedProfile?.let { profile ->
        AlertDialog(
            onDismissRequest = { selectedProfile = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val avatarRes = viewModel.itemsList.find { it.id == profile.profileImageId }?.resource 
                        ?: ArtRepository.artEntries.find { it.id == profile.profileImageId }?.resource
                    if (avatarRes != null) {
                        SpriteAnimation(painter = painterResource(avatarRes), frameCount = 3, modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(profile.username)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.achievements_title) + ": ${profile.achievements.size}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        profile.achievements.forEach { achId ->
                            val ach = viewModel.achievements.find { it.id == achId }
                            if (ach != null) {
                                Text(ach.emoji, fontSize = 24.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedProfile = null }) {
                    Text(stringResource(Res.string.close))
                }
            }
        )
    }
}

@Composable
fun PublicProfileCard(profile: PublicProfile, viewModel: GameViewModel, onClick: (PublicProfile) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(profile) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val avatarRes = viewModel.itemsList.find { it.id == profile.profileImageId }?.resource 
                ?: ArtRepository.artEntries.find { it.id == profile.profileImageId }?.resource
            if (avatarRes != null) {
                SpriteAnimation(painter = painterResource(avatarRes), frameCount = 3, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(profile.username, style = MaterialTheme.typography.titleMedium)
                Text("🏆 ${profile.achievements.size} ${stringResource(Res.string.achievements_title)}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun AchievementBadge(achievement: Achievement, isUnlocked: Boolean) {
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
