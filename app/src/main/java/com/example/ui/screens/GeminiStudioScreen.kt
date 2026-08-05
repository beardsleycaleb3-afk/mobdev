package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.GameForgeViewModel
import com.example.ui.theme.*

private val quickSuggestions = listOf(
    "Cyberpunk ninja dog dodging laser mice",
    "Space breakout with neon cosmic bricks",
    "Underwater maze runner collecting pearls",
    "Flappy dragon flying through lava pillars",
    "Retro synthwave racer with jump obstacles"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeminiStudioScreen(
    viewModel: GameForgeViewModel,
    modifier: Modifier = Modifier
) {
    val aiLog by viewModel.aiLog.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    var inputConcept by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PolishBackground)
            .padding(16.dp)
    ) {
        // Header Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PolishCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PolishPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = PolishOnPrimaryContainer)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AI Game Architect",
                        color = PolishTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Powered by Gemini 3.5 Flash",
                        color = PolishPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Quick Suggestion Chips
        Text(
            text = "Try an Idea Prompt:",
            color = PolishTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            quickSuggestions.forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PolishPrimaryContainer)
                        .clickable {
                            inputConcept = suggestion
                            viewModel.generateWithAI(suggestion)
                            inputConcept = ""
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "✨ $suggestion",
                        color = PolishOnPrimaryContainer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Chat Log
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(aiLog) { (msg, isUser) ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) PolishPrimary else PolishCardSurface
                        ),
                        border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, PolishBorder) else null,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg,
                                color = if (isUser) Color.White else PolishTextPrimary,
                                fontSize = 14.sp
                            )

                            if (!isUser && msg.startsWith("✨ Forged")) {
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.saveCurrentGame {
                                            viewModel.launchGame()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("ai_launch_game_button")
                                ) {
                                    Text("🎮 Test Play Now", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            if (isGenerating) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = PolishPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text("Gemini is forging your game physics & rules...", color = PolishTextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Input Prompt Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = inputConcept,
                onValueChange = { inputConcept = it },
                placeholder = { Text("Describe any game idea...", color = PolishTextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PolishTextPrimary,
                    unfocusedTextColor = PolishTextPrimary,
                    focusedContainerColor = PolishSurface,
                    unfocusedContainerColor = PolishSurface,
                    focusedBorderColor = PolishPrimary,
                    unfocusedBorderColor = PolishBorderDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_prompt_input")
            )

            IconButton(
                onClick = {
                    if (inputConcept.isNotBlank()) {
                        viewModel.generateWithAI(inputConcept)
                        inputConcept = ""
                    }
                },
                enabled = inputConcept.isNotBlank() && !isGenerating,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (inputConcept.isNotBlank()) PolishPrimary else PolishBorder
                    )
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

