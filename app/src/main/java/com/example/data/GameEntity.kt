package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val templateType: String, // RUNNER, BREAKOUT, SHOOTER, FLAPPY, MAZE
    val heroPrompt: String,
    val bgPrompt: String,
    val iconPrompt: String,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val speedMultiplier: Float = 1.0f,
    val gravity: Float = 1.0f,
    val playerSizeRatio: Float = 1.0f,
    val highScore: Int = 0,
    val playCount: Int = 0,
    val isFavorite: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)
