package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GameRepository(private val gameDao: GameDao) {

    val allGames: Flow<List<GameEntity>> = gameDao.getAllGames()
    val favoriteGames: Flow<List<GameEntity>> = gameDao.getFavoriteGames()

    suspend fun getGameById(id: Long): GameEntity? {
        return gameDao.getGameById(id)
    }

    suspend fun saveGame(game: GameEntity): Long {
        return gameDao.insertGame(game)
    }

    suspend fun updateGame(game: GameEntity) {
        gameDao.updateGame(game)
    }

    suspend fun deleteGame(game: GameEntity) {
        gameDao.deleteGame(game)
    }

    suspend fun updateHighScore(id: Long, highScore: Int) {
        gameDao.updateHighScore(id, highScore)
    }

    suspend fun incrementPlayCount(id: Long) {
        gameDao.incrementPlayCount(id)
    }

    suspend fun ensureInitialGames() {
        val currentGames = allGames.first()
        if (currentGames.isEmpty()) {
            val defaultGames = listOf(
                GameEntity(
                    title = "Pixel Dash",
                    templateType = "RUNNER",
                    heroPrompt = "cute chibi ninja fox",
                    bgPrompt = "sunset mountain silhouette",
                    iconPrompt = "runner fox icon",
                    primaryColorHex = "#6366F1",
                    secondaryColorHex = "#22D3EE",
                    speedMultiplier = 1.0f,
                    gravity = 1.0f,
                    highScore = 150
                ),
                GameEntity(
                    title = "Neon Breakout",
                    templateType = "BREAKOUT",
                    heroPrompt = "cyberpunk glowing paddle",
                    bgPrompt = "dark grid synthwave synth grid",
                    iconPrompt = "brick breaker icon",
                    primaryColorHex = "#EC4899",
                    secondaryColorHex = "#8B5CF6",
                    speedMultiplier = 1.2f,
                    gravity = 1.0f,
                    highScore = 320
                ),
                GameEntity(
                    title = "Nebula Blaster",
                    templateType = "SHOOTER",
                    heroPrompt = "neon starfighter jet",
                    bgPrompt = "deep galaxy space nebula stars",
                    iconPrompt = "rocket ship space icon",
                    primaryColorHex = "#06B6D4",
                    secondaryColorHex = "#F59E0B",
                    speedMultiplier = 1.1f,
                    gravity = 1.0f,
                    highScore = 480
                ),
                GameEntity(
                    title = "Cyber Flappy",
                    templateType = "FLAPPY",
                    heroPrompt = "retro pixel flap drone",
                    bgPrompt = "futuristic neon city sky",
                    iconPrompt = "flying drone icon",
                    primaryColorHex = "#10B981",
                    secondaryColorHex = "#3B82F6",
                    speedMultiplier = 1.0f,
                    gravity = 1.2f,
                    highScore = 85
                ),
                GameEntity(
                    title = "Maze Runner",
                    templateType = "MAZE",
                    heroPrompt = "glowing energy orb explorer",
                    bgPrompt = "ancient glowing labyrinth",
                    iconPrompt = "maze labyrinth icon",
                    primaryColorHex = "#F43F5E",
                    secondaryColorHex = "#10B981",
                    speedMultiplier = 1.0f,
                    gravity = 1.0f,
                    highScore = 210
                )
            )

            for (g in defaultGames) {
                gameDao.insertGame(g)
            }
        }
    }
}
