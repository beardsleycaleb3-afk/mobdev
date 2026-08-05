package com.example.game

import androidx.compose.ui.graphics.Color

enum class GameTemplateType(
    val displayName: String,
    val iconEmoji: String,
    val description: String
) {
    RUNNER("Runner", "🏃", "Side-scrolling endless jump & dodge action"),
    BREAKOUT("Breakout", "🧱", "Paddle & bouncing ball brick buster"),
    SHOOTER("Shooter", "🚀", "Vertical space shooter against alien waves"),
    FLAPPY("Flappy", "🐦", "Precision tapping flap & tube navigator"),
    MAZE("Maze Runner", "🌀", "Top-down labyrinth item collector"),
    PUZZLE("Color Puzzle", "🧩", "Grid-based matching & chain combo puzzle"),
    TOP_DOWN_SHOOTER("Arcade Shooter", "🕹️", "360-degree arena top-down shooter")
}

data class GameConfig(
    val id: Long = 0,
    val title: String = "Pixel Dash",
    val templateType: GameTemplateType = GameTemplateType.RUNNER,
    val heroPrompt: String = "cute chibi ninja fox",
    val bgPrompt: String = "sunset mountain silhouette",
    val iconPrompt: String = "game controller",
    val primaryColorHex: String = "#6366F1",
    val secondaryColorHex: String = "#22D3EE",
    val speedMultiplier: Float = 1.0f,
    val gravity: Float = 1.0f,
    val playerSizeRatio: Float = 1.0f,
    val lives: Int = 3
) {
    val primaryColor: Color
        get() = try {
            Color(android.graphics.Color.parseColor(primaryColorHex))
        } catch (e: Exception) {
            Color(0xFF6366F1)
        }

    val secondaryColor: Color
        get() = try {
            Color(android.graphics.Color.parseColor(secondaryColorHex))
        } catch (e: Exception) {
            Color(0xFF22D3EE)
        }
}

data class GameState(
    val score: Int = 0,
    val highScore: Int = 0,
    val lives: Int = 3,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val isVictory: Boolean = false,
    val combo: Int = 1,
    val elapsedSeconds: Float = 0f
)
