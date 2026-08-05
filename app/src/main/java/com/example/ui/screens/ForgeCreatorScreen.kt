package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameConfig
import com.example.game.GameTemplateType
import com.example.ui.GameForgeViewModel
import com.example.ui.theme.*

private val nameIdeas = listOf("Pixel Dash", "Astro Runner", "Brick Buster", "Nebula Blaster", "Cosmic Hopper", "Cyber Flap", "Labyrinth Escape")
private val heroIdeas = listOf("cute chibi ninja fox", "cheerful round robot", "brave knight girl", "neon cyberpunk samurai", "glowing starfighter jet")
private val bgIdeas = listOf("sunset mountain silhouette", "neon cyberpunk city street", "alien planet purple sky", "enchanted forest with glowing mushrooms", "deep space nebula starfield")

private val colorPalettes = listOf(
    Pair("#6750A4", "#EADDFF"), // Polish Purple & Light
    Pair("#10B981", "#3B82F6"), // Emerald & Blue
    Pair("#EC4899", "#8B5CF6"), // Neon Magenta & Violet
    Pair("#F59E0B", "#EF4444"), // Amber & Flame Red
    Pair("#06B6D4", "#F43F5E")  // Cyan & Crimson
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ForgeCreatorScreen(
    viewModel: GameForgeViewModel,
    modifier: Modifier = Modifier
) {
    val activeConfig by viewModel.activeConfig.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val scrollState = rememberScrollState()

    var gameTitle by remember(activeConfig) { mutableStateOf(activeConfig.title) }
    var selectedTemplate by remember(activeConfig) { mutableStateOf(activeConfig.templateType) }
    var heroPrompt by remember(activeConfig) { mutableStateOf(activeConfig.heroPrompt) }
    var bgPrompt by remember(activeConfig) { mutableStateOf(activeConfig.bgPrompt) }
    var speedMult by remember(activeConfig) { mutableFloatStateOf(activeConfig.speedMultiplier) }
    var gravMult by remember(activeConfig) { mutableFloatStateOf(activeConfig.gravity) }
    var selectedColorPair by remember(activeConfig) {
        mutableStateOf(Pair(activeConfig.primaryColorHex, activeConfig.secondaryColorHex))
    }

    // Helper to update VM config on change
    fun syncConfig() {
        viewModel.updateConfig(
            activeConfig.copy(
                title = gameTitle,
                templateType = selectedTemplate,
                heroPrompt = heroPrompt,
                bgPrompt = bgPrompt,
                primaryColorHex = selectedColorPair.first,
                secondaryColorHex = selectedColorPair.second,
                speedMultiplier = speedMult,
                gravity = gravMult
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PolishBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Progress Indicator & Top Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PolishCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GameForge Studio",
                            color = PolishTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Step 2 of 3 • Asset & Physics Engine",
                            color = PolishTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PolishPrimaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AI READY",
                            color = PolishOnPrimaryContainer,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Step Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(CircleShape).background(PolishPrimary))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(CircleShape).background(PolishPrimary))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(CircleShape).background(PolishPrimaryContainer))
                }
            }
        }

        // --- STEP 1: Name & Template Selector ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PolishCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. GAME TITLE & TEMPLATE",
                        fontWeight = FontWeight.Bold,
                        color = PolishPrimary,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                    Icon(Icons.Default.SportsEsports, contentDescription = null, tint = PolishPrimary)
                }

                // Name Input with Dice Randomizer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = gameTitle,
                        onValueChange = {
                            gameTitle = it
                            syncConfig()
                        },
                        label = { Text("Game Title", color = PolishTextSecondary) },
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
                            .testTag("game_name_input")
                    )

                    IconButton(
                        onClick = {
                            gameTitle = nameIdeas.random()
                            syncConfig()
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PolishPrimaryContainer)
                    ) {
                        Text("🎲", fontSize = 20.sp)
                    }
                }

                Text("Select Game Engine Template", color = PolishTextSecondary, fontSize = 13.sp)

                // Template Grid/Cards
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GameTemplateType.entries.forEach { template ->
                        val isSelected = selectedTemplate == template
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(PolishSurface)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) PolishPrimary else PolishBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedTemplate = template
                                    syncConfig()
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(template.iconEmoji, fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = template.displayName,
                                    color = if (isSelected) PolishPrimary else PolishTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                if (isSelected) {
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = PolishPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- STEP 2: Visual Prompts & Color Themes ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PolishCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. HERO & WORLD VISUALS",
                        fontWeight = FontWeight.Bold,
                        color = PolishPrimary,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                    Icon(Icons.Default.Palette, contentDescription = null, tint = PolishPrimary)
                }

                // Hero Sprite Prompt
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = heroPrompt,
                        onValueChange = {
                            heroPrompt = it
                            syncConfig()
                        },
                        label = { Text("Hero Sprite Description", color = PolishTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PolishTextPrimary,
                            unfocusedTextColor = PolishTextPrimary,
                            focusedContainerColor = PolishSurface,
                            unfocusedContainerColor = PolishSurface,
                            focusedBorderColor = PolishPrimary,
                            unfocusedBorderColor = PolishBorderDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            heroPrompt = heroIdeas.random()
                            syncConfig()
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PolishPrimaryContainer)
                    ) {
                        Text("🎲", fontSize = 20.sp)
                    }
                }

                // Background World Prompt
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = bgPrompt,
                        onValueChange = {
                            bgPrompt = it
                            syncConfig()
                        },
                        label = { Text("World / Background Style", color = PolishTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PolishTextPrimary,
                            unfocusedTextColor = PolishTextPrimary,
                            focusedContainerColor = PolishSurface,
                            unfocusedContainerColor = PolishSurface,
                            focusedBorderColor = PolishPrimary,
                            unfocusedBorderColor = PolishBorderDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            bgPrompt = bgIdeas.random()
                            syncConfig()
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PolishPrimaryContainer)
                    ) {
                        Text("🎲", fontSize = 20.sp)
                    }
                }

                Text("Theme Color Palette", color = PolishTextSecondary, fontSize = 13.sp)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorPalettes.forEach { pair ->
                        val isSel = selectedColorPair == pair
                        val c1 = try { Color(android.graphics.Color.parseColor(pair.first)) } catch (e: Exception) { Color.Cyan }
                        val c2 = try { Color(android.graphics.Color.parseColor(pair.second)) } catch (e: Exception) { Color.Blue }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(colors = listOf(c1, c2))
                                )
                                .border(
                                    width = if (isSel) 3.dp else 1.dp,
                                    color = if (isSel) PolishPrimary else PolishBorder,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedColorPair = pair
                                    syncConfig()
                                }
                        )
                    }
                }
            }
        }

        // --- STEP 3: Physics & Difficulty Tweaker ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PolishCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3. PHYSICS & RULE TUNING",
                        fontWeight = FontWeight.Bold,
                        color = PolishPrimary,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                    Icon(Icons.Default.Speed, contentDescription = null, tint = PolishPrimary)
                }

                // Speed Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Game Speed", color = PolishTextSecondary, fontSize = 13.sp)
                        Text("${"%.1f".format(speedMult)}x", color = PolishPrimary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = speedMult,
                        onValueChange = {
                            speedMult = it
                            syncConfig()
                        },
                        valueRange = 0.5f..2.2f,
                        colors = SliderDefaults.colors(
                            thumbColor = PolishPrimary,
                            activeTrackColor = PolishPrimary,
                            inactiveTrackColor = PolishBorder
                        )
                    )
                }

                // Gravity Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gravity Force", color = PolishTextSecondary, fontSize = 13.sp)
                        Text("${"%.1f".format(gravMult)}x", color = PolishPrimary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = gravMult,
                        onValueChange = {
                            gravMult = it
                            syncConfig()
                        },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = PolishPrimary,
                            activeTrackColor = PolishPrimary,
                            inactiveTrackColor = PolishBorder
                        )
                    )
                }
            }
        }

        // --- Action Buttons ---
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    syncConfig()
                    viewModel.saveCurrentGame {
                        viewModel.launchGame()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("launch_game_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Generate & Preview Game", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Button(
                onClick = {
                    syncConfig()
                    viewModel.saveCurrentGame()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_game_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimaryContainer)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save", tint = PolishOnPrimaryContainer)
                Spacer(Modifier.width(8.dp))
                Text("Save to Arcade Library", color = PolishOnPrimaryContainer, fontWeight = FontWeight.Bold)
            }
        }
    }
}

