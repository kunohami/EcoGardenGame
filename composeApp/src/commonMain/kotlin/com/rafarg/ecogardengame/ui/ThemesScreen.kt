package com.rafarg.ecogardengame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rafarg.ecogardengame.viewmodel.GameViewModel

@Composable
fun ThemesScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(16.dp).fillMaxSize()
        ) {
            Text("Themes", style = MaterialTheme.typography.displayMedium)

            Spacer(modifier = Modifier.height(8.dp))

            // Light Theme Option
            ThemeCard(
                name = "Dry Sage (Default)",
                colors = listOf(SageColor, BeigeColor, CornsilkColor, PapayaColor, BronzeColor),
                isSelected = !viewModel.isDarkTheme,
                onClick = { viewModel.setTheme(false) }
            )

            // Dark Theme Option
            ThemeCard(
                name = "Ink Black",
                colors = listOf(InkBlack, JetBlack, CharcoalBlue, DeepTeal, DarkSlateGrey, Evergreen),
                isSelected = viewModel.isDarkTheme,
                onClick = { viewModel.setTheme(true) }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(onClick = onBack, modifier = Modifier.fillMaxWidth(0.6f)) {
                Text("Back to Misc")
            }
        }
    }
}

@Composable
fun ThemeCard(
    name: String,
    colors: List<Color>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    colors.take(5).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
            }
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}
