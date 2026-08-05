package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameForgeViewModel
import com.example.ui.theme.*

@Composable
fun StatsScreen(
    viewModel: GameForgeViewModel,
    modifier: Modifier = Modifier
) {
    val allGames by viewModel.allGames.collectAsState()
    val scrollState = rememberScrollState()

    val totalGames = allGames.size
    val totalPlays = allGames.sumOf { it.playCount }
    val maxScore = allGames.maxOfOrNull { it.highScore } ?: 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PolishBackground)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Creator Analytics",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PolishTextPrimary
        )

        // Stat Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(
                title = "Total Games",
                value = "$totalGames",
                icon = "🎮",
                color = PolishPrimary,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                title = "Max Score",
                value = "$maxScore",
                icon = "🏆",
                color = PolishPrimary,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                title = "Total Plays",
                value = "$totalPlays",
                icon = "🚀",
                color = PolishPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Unlocked Creator Badges",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PolishTextPrimary
        )

        // Achievement Badges
        AchievementItem(
            title = "Master Game Creator",
            desc = "Created 3 or more custom games",
            emoji = "🔨",
            isUnlocked = totalGames >= 3
        )

        AchievementItem(
            title = "High Scorer (200+)",
            desc = "Achieved 200+ points in any game",
            emoji = "⭐",
            isUnlocked = maxScore >= 200
        )

        AchievementItem(
            title = "Arcade Veteran",
            desc = "Completed 10 or more play test runs",
            emoji = "🎖️",
            isUnlocked = totalPlays >= 10
        )

        AchievementItem(
            title = "AI Prompt Specialist",
            desc = "Forged a game concept with Gemini AI",
            emoji = "✨",
            isUnlocked = true
        )
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PolishCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(icon, fontSize = 28.sp)
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = PolishTextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AchievementItem(
    title: String,
    desc: String,
    emoji: String,
    isUnlocked: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) PolishCardSurface else PolishCardSurface.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isUnlocked) PolishPrimaryContainer else PolishBorder
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 24.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) PolishTextPrimary else PolishTextSecondary,
                    fontSize = 15.sp
                )
                Text(
                    text = desc,
                    color = PolishTextSecondary,
                    fontSize = 12.sp
                )
            }

            if (isUnlocked) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Unlocked",
                    tint = PolishPrimary
                )
            }
        }
    }
}

