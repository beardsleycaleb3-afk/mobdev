package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameEntity
import com.example.game.GameTemplateType
import com.example.ui.GameForgeViewModel
import com.example.ui.theme.*

@Composable
fun ArcadeLibraryScreen(
    viewModel: GameForgeViewModel,
    modifier: Modifier = Modifier
) {
    val allGames by viewModel.allGames.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PolishBackground)
            .padding(16.dp)
    ) {
        // Title Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Arcade Library",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )
                Text(
                    text = "${allGames.size} Games Ready to Play",
                    color = PolishTextSecondary,
                    fontSize = 13.sp
                )
            }

            IconButton(
                onClick = { viewModel.setTab(0) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(PolishPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Game", tint = Color.White)
            }
        }

        if (allGames.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎮", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No Games Saved Yet",
                        color = PolishTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Forge your first game in the Studio tab!",
                        color = PolishTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(allGames, key = { it.id }) { game ->
                    GameCardItem(
                        game = game,
                        onPlay = {
                            viewModel.loadEntityToConfig(game)
                            viewModel.launchGame()
                        },
                        onEdit = {
                            viewModel.loadEntityToConfig(game)
                            viewModel.setTab(0)
                        },
                        onDelete = {
                            viewModel.deleteGame(game)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GameCardItem(
    game: GameEntity,
    onPlay: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val template = try {
        GameTemplateType.valueOf(game.templateType)
    } catch (e: Exception) {
        GameTemplateType.RUNNER
    }

    val primaryCol = try {
        Color(android.graphics.Color.parseColor(game.primaryColorHex))
    } catch (e: Exception) {
        PolishPrimary
    }

    val secondaryCol = try {
        Color(android.graphics.Color.parseColor(game.secondaryColorHex))
    } catch (e: Exception) {
        PolishPrimaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_card_${game.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PolishCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(colors = listOf(primaryCol, secondaryCol))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(template.iconEmoji, fontSize = 42.sp)

                // High Score Badge
                if (game.highScore > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🏆 ${game.highScore}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Title & Subtitle
            Column {
                Text(
                    text = game.title,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Text(
                    text = template.displayName,
                    color = PolishTextSecondary,
                    fontSize = 12.sp
                )
            }

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PolishTextSecondary, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFB3261E), modifier = Modifier.size(18.dp))
                }

                Button(
                    onClick = onPlay,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("play_button_${game.id}"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
                ) {
                    Text("Play", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

