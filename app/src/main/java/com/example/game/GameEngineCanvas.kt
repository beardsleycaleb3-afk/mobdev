package com.example.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import java.util.Random
import kotlin.math.*

// Particle effect for visual juice
private data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var radius: Float,
    var alpha: Float = 1.0f,
    var life: Float = 1.0f
)

// Internal entity structures for physics engine
private data class Obstacle(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
    var type: Int = 0, // 0 = ground, 1 = aerial, 2 = coin
    var color: Color = Color.Red,
    var collected: Boolean = false
)

private data class Brick(
    var rect: Rect,
    var color: Color,
    var hp: Int = 1
)

private data class Laser(
    var x: Float,
    var y: Float,
    var vy: Float = -15f,
    var isEnemy: Boolean = false
)

private data class Alien(
    var x: Float,
    var y: Float,
    var radius: Float,
    var vx: Float,
    var hp: Int,
    var maxHp: Int
)

private data class Pipe(
    var x: Float,
    var gapY: Float,
    var gapHeight: Float,
    var width: Float,
    var passed: Boolean = false
)

private data class MazeGem(
    var x: Float,
    var y: Float,
    var collected: Boolean = false
)

private data class MazeGuard(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float
)

// Data class for Puzzle Game
private data class PuzzleTile(
    val row: Int,
    val col: Int,
    var colorIndex: Int
)

// Data class for Top Down Shooter
private data class TopDownEnemy(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var hp: Int,
    var radius: Float,
    var color: Color
)

private data class TopDownBullet(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float
)

@Composable
fun GameEngineCanvas(
    config: GameConfig,
    onScoreUpdate: (Int) -> Unit,
    onGameOver: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var gameState by remember(config) {
        mutableStateOf(GameState(lives = config.lives))
    }

    // Engine States
    var canvasSize by remember { mutableStateOf(Size(1000f, 1600f)) }
    var isMuted by remember { mutableStateOf(SoundManager.isMuted()) }

    // Runner physics state
    var playerY by remember { mutableStateOf(0f) }
    var playerVelocityY by remember { mutableStateOf(0f) }
    var isJumping by remember { mutableStateOf(false) }
    var jumpCount by remember { mutableStateOf(0) }
    var runnerObstacles by remember { mutableStateOf(listOf<Obstacle>()) }

    // Breakout physics state
    var paddleX by remember { mutableStateOf(400f) }
    var ballX by remember { mutableStateOf(500f) }
    var ballY by remember { mutableStateOf(1000f) }
    var ballVx by remember { mutableStateOf(6f) }
    var ballVy by remember { mutableStateOf(-8f) }
    var breakoutBricks by remember { mutableStateOf(listOf<Brick>()) }

    // Shooter physics state
    var shipX by remember { mutableStateOf(500f) }
    var shipY by remember { mutableStateOf(1300f) }
    var lasers by remember { mutableStateOf(listOf<Laser>()) }
    var aliens by remember { mutableStateOf(listOf<Alien>()) }

    // Flappy physics state
    var flappyY by remember { mutableStateOf(600f) }
    var flappyVy by remember { mutableStateOf(0f) }
    var flappyPipes by remember { mutableStateOf(listOf<Pipe>()) }

    // Maze physics state
    var heroMazeX by remember { mutableStateOf(100f) }
    var heroMazeY by remember { mutableStateOf(100f) }
    var mazeGems by remember { mutableStateOf(listOf<MazeGem>()) }
    var mazeGuards by remember { mutableStateOf(listOf<MazeGuard>()) }

    // Puzzle physics state
    val puzzleGridSize = 4
    var puzzleGrid by remember { mutableStateOf(listOf<PuzzleTile>()) }
    val puzzleColors = remember {
        listOf(
            Color(0xFFF43F5E), // Red
            Color(0xFF3B82F6), // Blue
            Color(0xFF10B981), // Emerald
            Color(0xFFF59E0B), // Amber
            Color(0xFF8B5CF6)  // Purple
        )
    }

    // Top-Down Arcade Shooter state
    var tdPlayerX by remember { mutableStateOf(500f) }
    var tdPlayerY by remember { mutableStateOf(800f) }
    var tdBullets by remember { mutableStateOf(listOf<TopDownBullet>()) }
    var tdEnemies by remember { mutableStateOf(listOf<TopDownEnemy>()) }

    // Visual particles
    var particles by remember { mutableStateOf(listOf<Particle>()) }
    val random = remember { Random() }

    // Start background music
    DisposableEffect(Unit) {
        SoundManager.startBgm()
        onDispose {
            SoundManager.stopBgm()
        }
    }

    // Initialize level layout when canvasSize is determined
    fun resetGame() {
        gameState = GameState(lives = config.lives)
        val w = canvasSize.width.coerceAtLeast(400f)
        val h = canvasSize.height.coerceAtLeast(600f)

        when (config.templateType) {
            GameTemplateType.RUNNER -> {
                playerY = h - 250f
                playerVelocityY = 0f
                isJumping = false
                jumpCount = 0
                runnerObstacles = emptyList()
            }
            GameTemplateType.BREAKOUT -> {
                paddleX = w / 2f
                ballX = w / 2f
                ballY = h - 300f
                ballVx = 6f * config.speedMultiplier
                ballVy = -8f * config.speedMultiplier
                
                val bricksList = mutableListOf<Brick>()
                val rows = 5
                val cols = 6
                val bWidth = (w - 60f) / cols
                val bHeight = 40f
                val colors = listOf(Color(0xFFF43F5E), Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF8B5CF6))

                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val rect = Rect(30f + c * bWidth + 4f, 150f + r * (bHeight + 8f), 30f + (c + 1) * bWidth - 4f, 150f + r * (bHeight + 8f) + bHeight)
                        bricksList.add(Brick(rect, colors[r % colors.size]))
                    }
                }
                breakoutBricks = bricksList
            }
            GameTemplateType.SHOOTER -> {
                shipX = w / 2f
                shipY = h - 200f
                lasers = emptyList()
                aliens = emptyList()
            }
            GameTemplateType.FLAPPY -> {
                flappyY = h / 2f
                flappyVy = 0f
                flappyPipes = emptyList()
            }
            GameTemplateType.MAZE -> {
                heroMazeX = 100f
                heroMazeY = 100f
                val gems = mutableListOf<MazeGem>()
                for (i in 0 until 12) {
                    gems.add(MazeGem(100f + (i % 4) * (w - 200f) / 3, 200f + (i / 4) * (h - 400f) / 3))
                }
                mazeGems = gems
                mazeGuards = listOf(
                    MazeGuard(w / 2f, h / 2f, 3f, 0f),
                    MazeGuard(w / 3f, h / 3f, 0f, 3f)
                )
            }
            GameTemplateType.PUZZLE -> {
                val tiles = mutableListOf<PuzzleTile>()
                for (r in 0 until puzzleGridSize) {
                    for (c in 0 until puzzleGridSize) {
                        tiles.add(PuzzleTile(r, c, random.nextInt(puzzleColors.size)))
                    }
                }
                puzzleGrid = tiles
            }
            GameTemplateType.TOP_DOWN_SHOOTER -> {
                tdPlayerX = w / 2f
                tdPlayerY = h / 2f
                tdBullets = emptyList()
                tdEnemies = emptyList()
            }
        }
        particles = emptyList()
    }

    // Helper to spawn explosion particles
    fun addParticles(x: Float, y: Float, color: Color, count: Int = 15) {
        val newParticles = particles.toMutableList()
        for (i in 0 until count) {
            val angle = random.nextFloat() * 2 * PI.toFloat()
            val speed = random.nextFloat() * 8f + 2f
            newParticles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    radius = random.nextFloat() * 6f + 3f
                )
            )
        }
        particles = newParticles.take(50)
    }

    // Puzzle Tile Tap Handler
    fun onPuzzleTileTap(row: Int, col: Int) {
        val clickedTile = puzzleGrid.find { it.row == row && it.col == col } ?: return
        val targetColor = clickedTile.colorIndex

        // Find connected tiles with same color
        val matchingTiles = mutableSetOf<Pair<Int, Int>>()
        val queue = mutableListOf(Pair(row, col))
        matchingTiles.add(Pair(row, col))

        val directions = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))

        while (queue.isNotEmpty()) {
            val (r, c) = queue.removeAt(0)
            for ((dr, dc) in directions) {
                val nr = r + dr
                val nc = c + dc
                if (nr in 0 until puzzleGridSize && nc in 0 until puzzleGridSize) {
                    val p = Pair(nr, nc)
                    if (!matchingTiles.contains(p)) {
                        val neighbor = puzzleGrid.find { it.row == nr && it.col == nc }
                        if (neighbor != null && neighbor.colorIndex == targetColor) {
                            matchingTiles.add(p)
                            queue.add(p)
                        }
                    }
                }
            }
        }

        if (matchingTiles.size >= 2) {
            // Sound & particles
            SoundManager.playPuzzlePop()
            SoundManager.playScore()
            val points = matchingTiles.size * 15
            val newScore = gameState.score + points
            gameState = gameState.copy(score = newScore)
            onScoreUpdate(newScore)

            // Replace matched tiles with new random colors
            puzzleGrid = puzzleGrid.map { tile ->
                if (matchingTiles.contains(Pair(tile.row, tile.col))) {
                    val w = canvasSize.width
                    val h = canvasSize.height
                    val tileSize = (w - 80f) / puzzleGridSize
                    val startX = 40f
                    val startY = (h - (tileSize * puzzleGridSize)) / 2f
                    val x = startX + tile.col * tileSize + tileSize / 2f
                    val y = startY + tile.row * tileSize + tileSize / 2f
                    addParticles(x, y, puzzleColors[targetColor], 10)

                    tile.copy(colorIndex = random.nextInt(puzzleColors.size))
                } else tile
            }
        }
    }

    // Main 60 FPS Game Physics Engine Loop
    LaunchedEffect(config, gameState.isPaused, gameState.isGameOver) {
        if (gameState.isPaused || gameState.isGameOver) return@LaunchedEffect

        var lastFrameTime = System.nanoTime()

        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                val dt = ((frameTimeNanos - lastFrameTime) / 1e9f).coerceIn(0.001f, 0.05f)
                lastFrameTime = frameTimeNanos

                if (canvasSize.width <= 0f) return@withFrameNanos

                val w = canvasSize.width
                val h = canvasSize.height
                val speed = config.speedMultiplier
                val gravityFactor = config.gravity

                // Update elapsed time
                gameState = gameState.copy(elapsedSeconds = gameState.elapsedSeconds + dt)

                // Update particles
                particles = particles.mapNotNull { p ->
                    p.x += p.vx
                    p.y += p.vy
                    p.life -= 0.03f
                    p.alpha = p.life.coerceIn(0f, 1f)
                    if (p.life > 0) p else null
                }

                when (config.templateType) {
                    // -------------------------------------------------------------
                    // 1. RUNNER PHYSICS
                    // -------------------------------------------------------------
                    GameTemplateType.RUNNER -> {
                        val groundY = h - 200f
                        playerY += playerVelocityY * dt * 60f
                        playerVelocityY += 0.8f * gravityFactor * dt * 60f

                        if (playerY >= groundY) {
                            playerY = groundY
                            playerVelocityY = 0f
                            isJumping = false
                            jumpCount = 0
                        }

                        // Spawn obstacles
                        if (random.nextFloat() < 0.02f * speed && runnerObstacles.size < 4) {
                            val isAerial = random.nextBoolean()
                            val obsY = if (isAerial) groundY - 120f else groundY
                            val isCoin = random.nextFloat() < 0.3f
                            val color = if (isCoin) Color(0xFFF59E0B) else config.primaryColor

                            runnerObstacles = runnerObstacles + Obstacle(
                                x = w + 50f,
                                y = obsY,
                                width = if (isCoin) 30f else 50f,
                                height = if (isCoin) 30f else 60f,
                                type = if (isCoin) 2 else (if (isAerial) 1 else 0),
                                color = color
                            )
                        }

                        // Move obstacles & detect collision
                        val updatedObs = mutableListOf<Obstacle>()
                        val playerRect = Rect(100f, playerY - 50f, 150f, playerY)

                        for (obs in runnerObstacles) {
                            obs.x -= (6f * speed)
                            val obsRect = Rect(obs.x, obs.y - obs.height, obs.x + obs.width, obs.y)

                            if (playerRect.overlaps(obsRect)) {
                                if (obs.type == 2 && !obs.collected) {
                                    obs.collected = true
                                    val newScore = gameState.score + 20
                                    gameState = gameState.copy(score = newScore)
                                    onScoreUpdate(newScore)
                                    SoundManager.playScore()
                                    addParticles(obs.x, obs.y, Color(0xFFF59E0B), 10)
                                } else if (obs.type != 2) {
                                    // Hit obstacle
                                    SoundManager.playCollision()
                                    addParticles(playerRect.center.x, playerRect.center.y, Color.Red, 20)
                                    val newLives = gameState.lives - 1
                                    if (newLives <= 0) {
                                        SoundManager.playGameOver()
                                        gameState = gameState.copy(lives = 0, isGameOver = true)
                                        onGameOver(gameState.score)
                                    } else {
                                        gameState = gameState.copy(lives = newLives)
                                    }
                                    continue // Destroy obstacle on hit
                                }
                            }

                            if (obs.x + obs.width > -50f && !obs.collected) {
                                updatedObs.add(obs)
                            } else if (!obs.collected && obs.type != 2) {
                                // Passed obstacle safely
                                val newScore = gameState.score + 5
                                gameState = gameState.copy(score = newScore)
                                onScoreUpdate(newScore)
                            }
                        }
                        runnerObstacles = updatedObs
                    }

                    // -------------------------------------------------------------
                    // 2. BREAKOUT PHYSICS
                    // -------------------------------------------------------------
                    GameTemplateType.BREAKOUT -> {
                        ballX += ballVx
                        ballY += ballVy

                        val pWidth = 120f * config.playerSizeRatio
                        val paddleRect = Rect(paddleX - pWidth / 2, h - 180f, paddleX + pWidth / 2, h - 150f)
                        val ballRadius = 12f

                        // Wall Bounces
                        if (ballX - ballRadius <= 0f || ballX + ballRadius >= w) ballVx = -ballVx
                        if (ballY - ballRadius <= 100f) ballVy = -ballVy

                        // Paddle Collision
                        if (ballY + ballRadius >= paddleRect.top && ballY - ballRadius <= paddleRect.bottom &&
                            ballX >= paddleRect.left && ballX <= paddleRect.right
                        ) {
                            ballVy = -abs(ballVy)
                            // Angle deflection based on hit position
                            val hitPoint = (ballX - paddleX) / (pWidth / 2)
                            ballVx = hitPoint * 8f * speed
                            SoundManager.playJump()
                            addParticles(ballX, ballY, config.secondaryColor, 8)
                        }

                        // Bottom out
                        if (ballY >= h - 100f) {
                            val newLives = gameState.lives - 1
                            SoundManager.playCollision()
                            addParticles(ballX, ballY, Color.Red, 25)
                            if (newLives <= 0) {
                                SoundManager.playGameOver()
                                gameState = gameState.copy(lives = 0, isGameOver = true)
                                onGameOver(gameState.score)
                            } else {
                                gameState = gameState.copy(lives = newLives)
                                ballX = paddleX
                                ballY = h - 300f
                                ballVy = -8f * speed
                            }
                        }

                        // Brick Collisions
                        val updatedBricks = mutableListOf<Brick>()
                        for (b in breakoutBricks) {
                            val ballRect = Rect(ballX - ballRadius, ballY - ballRadius, ballX + ballRadius, ballY + ballRadius)
                            if (ballRect.overlaps(b.rect)) {
                                ballVy = -ballVy
                                val newScore = gameState.score + 10
                                gameState = gameState.copy(score = newScore)
                                onScoreUpdate(newScore)
                                SoundManager.playScore()
                                addParticles(b.rect.center.x, b.rect.center.y, b.color, 12)
                            } else {
                                updatedBricks.add(b)
                            }
                        }
                        breakoutBricks = updatedBricks

                        if (breakoutBricks.isEmpty()) {
                            SoundManager.playVictory()
                            gameState = gameState.copy(isVictory = true, isGameOver = true)
                            onGameOver(gameState.score + 100)
                        }
                    }

                    // -------------------------------------------------------------
                    // 3. SHOOTER PHYSICS
                    // -------------------------------------------------------------
                    GameTemplateType.SHOOTER -> {
                        // Auto fire lasers
                        if (random.nextFloat() < 0.15f) {
                            lasers = lasers + Laser(shipX, shipY - 30f, vy = -18f * speed)
                            SoundManager.playShoot()
                        }

                        // Spawn Aliens
                        if (random.nextFloat() < 0.03f * speed && aliens.size < 8) {
                            aliens = aliens + Alien(
                                x = random.nextFloat() * (w - 100f) + 50f,
                                y = 100f,
                                radius = 25f,
                                vx = (random.nextFloat() - 0.5f) * 4f,
                                hp = 2,
                                maxHp = 2
                            )
                        }

                        // Move Lasers
                        lasers = lasers.mapNotNull { l ->
                            l.y += l.vy
                            if (l.y > 0f && l.y < h) l else null
                        }

                        // Move Aliens & Collision
                        val remainingAliens = mutableListOf<Alien>()
                        val currentLasers = lasers.toMutableList()

                        for (alien in aliens) {
                            alien.x = (alien.x + alien.vx).coerceIn(30f, w - 30f)
                            alien.y += 2f * speed

                            val alienRect = Rect(alien.x - alien.radius, alien.y - alien.radius, alien.x + alien.radius, alien.y + alien.radius)

                            // Hit by laser
                            var hit = false
                            val laserIter = currentLasers.iterator()
                            while (laserIter.hasNext()) {
                                val l = laserIter.next()
                                if (alienRect.contains(Offset(l.x, l.y))) {
                                    laserIter.remove()
                                    alien.hp -= 1
                                    addParticles(l.x, l.y, Color(0xFFF59E0B), 6)
                                    if (alien.hp <= 0) {
                                        hit = true
                                        val newScore = gameState.score + 25
                                        gameState = gameState.copy(score = newScore)
                                        onScoreUpdate(newScore)
                                        SoundManager.playScore()
                                        addParticles(alien.x, alien.y, config.secondaryColor, 15)
                                    }
                                    break
                                }
                            }

                            if (!hit) {
                                if (alien.y >= h - 150f) {
                                    // Alien reached ship area
                                    val newLives = gameState.lives - 1
                                    SoundManager.playCollision()
                                    addParticles(shipX, shipY, Color.Red, 20)
                                    if (newLives <= 0) {
                                        SoundManager.playGameOver()
                                        gameState = gameState.copy(lives = 0, isGameOver = true)
                                        onGameOver(gameState.score)
                                    } else {
                                        gameState = gameState.copy(lives = newLives)
                                    }
                                } else {
                                    remainingAliens.add(alien)
                                }
                            }
                        }
                        aliens = remainingAliens
                        lasers = currentLasers
                    }

                    // -------------------------------------------------------------
                    // 4. FLAPPY PHYSICS
                    // -------------------------------------------------------------
                    GameTemplateType.FLAPPY -> {
                        flappyY += flappyVy * dt * 60f
                        flappyVy += 0.7f * gravityFactor * dt * 60f

                        // Pipe Spawning
                        if (random.nextFloat() < 0.015f * speed && flappyPipes.size < 4) {
                            val gapH = 220f
                            val gapY = random.nextFloat() * (h - 400f) + 150f
                            flappyPipes = flappyPipes + Pipe(w + 50f, gapY, gapH, 70f)
                        }

                        // Pipe Movement & Collision
                        val birdRect = Rect(120f, flappyY - 20f, 160f, flappyY + 20f)
                        val updatedPipes = mutableListOf<Pipe>()

                        for (p in flappyPipes) {
                            p.x -= 4f * speed

                            val topPipeRect = Rect(p.x, 0f, p.x + p.width, p.gapY)
                            val bottomPipeRect = Rect(p.x, p.gapY + p.gapHeight, p.x + p.width, h)

                            if (birdRect.overlaps(topPipeRect) || birdRect.overlaps(bottomPipeRect) || flappyY >= h - 100f || flappyY <= 50f) {
                                SoundManager.playCollision()
                                SoundManager.playGameOver()
                                addParticles(140f, flappyY, Color.Red, 25)
                                gameState = gameState.copy(lives = 0, isGameOver = true)
                                onGameOver(gameState.score)
                                break
                            }

                            if (!p.passed && p.x + p.width < 120f) {
                                p.passed = true
                                val newScore = gameState.score + 10
                                gameState = gameState.copy(score = newScore)
                                onScoreUpdate(newScore)
                                SoundManager.playScore()
                            }

                            if (p.x + p.width > -50f) {
                                updatedPipes.add(p)
                            }
                        }
                        flappyPipes = updatedPipes
                    }

                    // -------------------------------------------------------------
                    // 5. MAZE PHYSICS
                    // -------------------------------------------------------------
                    GameTemplateType.MAZE -> {
                        // Collect gems
                        val updatedGems = mazeGems.map { gem ->
                            if (!gem.collected && hypot(heroMazeX - gem.x, heroMazeY - gem.y) < 35f) {
                                val newScore = gameState.score + 15
                                gameState = gameState.copy(score = newScore)
                                onScoreUpdate(newScore)
                                SoundManager.playScore()
                                addParticles(gem.x, gem.y, Color(0xFF10B981), 10)
                                gem.copy(collected = true)
                            } else gem
                        }
                        mazeGems = updatedGems

                        // Move Guards
                        mazeGuards = mazeGuards.map { guard ->
                            var nx = guard.x + guard.vx * speed
                            var ny = guard.y + guard.vy * speed
                            if (nx < 50f || nx > w - 50f) guard.vx = -guard.vx
                            if (ny < 150f || ny > h - 150f) guard.vy = -guard.vy

                            // Check player hit
                            if (hypot(heroMazeX - guard.x, heroMazeY - guard.y) < 35f) {
                                val newLives = gameState.lives - 1
                                SoundManager.playCollision()
                                addParticles(heroMazeX, heroMazeY, Color.Red, 20)
                                if (newLives <= 0) {
                                    SoundManager.playGameOver()
                                    gameState = gameState.copy(lives = 0, isGameOver = true)
                                    onGameOver(gameState.score)
                                } else {
                                    gameState = gameState.copy(lives = newLives)
                                    heroMazeX = 100f
                                    heroMazeY = 100f
                                }
                            }
                            guard.copy(x = nx.coerceIn(50f, w - 50f), y = ny.coerceIn(150f, h - 150f))
                        }

                        if (mazeGems.all { it.collected }) {
                            SoundManager.playVictory()
                            gameState = gameState.copy(isVictory = true, isGameOver = true)
                            onGameOver(gameState.score + 150)
                        }
                    }

                    // -------------------------------------------------------------
                    // 6. PUZZLE PHYSICS
                    // -------------------------------------------------------------
                    GameTemplateType.PUZZLE -> {
                        // Passive time check or score win condition
                        if (gameState.score >= 300) {
                            SoundManager.playVictory()
                            gameState = gameState.copy(isVictory = true, isGameOver = true)
                            onGameOver(gameState.score)
                        }
                    }

                    // -------------------------------------------------------------
                    // 7. TOP DOWN SHOOTER PHYSICS
                    // -------------------------------------------------------------
                    GameTemplateType.TOP_DOWN_SHOOTER -> {
                        // Spawn enemies from screen edges
                        if (random.nextFloat() < 0.04f * speed && tdEnemies.size < 10) {
                            val side = random.nextInt(4)
                            val (ex, ey) = when (side) {
                                0 -> Pair(-30f, random.nextFloat() * h) // Left
                                1 -> Pair(w + 30f, random.nextFloat() * h) // Right
                                2 -> Pair(random.nextFloat() * w, -30f) // Top
                                else -> Pair(random.nextFloat() * w, h + 30f) // Bottom
                            }

                            val dx = tdPlayerX - ex
                            val dy = tdPlayerY - ey
                            val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                            val enemySpeed = (2.5f + random.nextFloat() * 2f) * speed
                            val evx = (dx / dist) * enemySpeed
                            val evy = (dy / dist) * enemySpeed

                            tdEnemies = tdEnemies + TopDownEnemy(
                                x = ex,
                                y = ey,
                                vx = evx,
                                vy = evy,
                                hp = 2,
                                radius = 22f,
                                color = Color(0xFFEC4899)
                            )
                        }

                        // Auto-fire bullets
                        if (random.nextFloat() < 0.12f) {
                            tdBullets = tdBullets + TopDownBullet(
                                x = tdPlayerX,
                                y = tdPlayerY,
                                vx = 0f,
                                vy = -16f * speed
                            )
                            SoundManager.playShoot()
                        }

                        // Move Bullets
                        tdBullets = tdBullets.mapNotNull { b ->
                            val nx = b.x + b.vx
                            val ny = b.y + b.vy
                            if (nx in 0f..w && ny in 0f..h) b.copy(x = nx, y = ny) else null
                        }

                        // Move Enemies & Collisions
                        val remainingEnemies = mutableListOf<TopDownEnemy>()
                        val currentBullets = tdBullets.toMutableList()

                        for (enemy in tdEnemies) {
                            // Update direction towards player
                            val dx = tdPlayerX - enemy.x
                            val dy = tdPlayerY - enemy.y
                            val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                            val enemySpeed = 3f * speed
                            enemy.x += (dx / dist) * enemySpeed
                            enemy.y += (dy / dist) * enemySpeed

                            // Check player collision
                            if (dist < (enemy.radius + 20f)) {
                                val newLives = gameState.lives - 1
                                SoundManager.playCollision()
                                addParticles(tdPlayerX, tdPlayerY, Color.Red, 20)
                                if (newLives <= 0) {
                                    SoundManager.playGameOver()
                                    gameState = gameState.copy(lives = 0, isGameOver = true)
                                    onGameOver(gameState.score)
                                } else {
                                    gameState = gameState.copy(lives = newLives)
                                }
                                continue
                            }

                            // Check bullet collision
                            var destroyed = false
                            val bIter = currentBullets.iterator()
                            while (bIter.hasNext()) {
                                val b = bIter.next()
                                if (hypot(b.x - enemy.x, b.y - enemy.y) < enemy.radius) {
                                    bIter.remove()
                                    enemy.hp -= 1
                                    addParticles(b.x, b.y, Color(0xFFF59E0B), 5)
                                    if (enemy.hp <= 0) {
                                        destroyed = true
                                        val newScore = gameState.score + 20
                                        gameState = gameState.copy(score = newScore)
                                        onScoreUpdate(newScore)
                                        SoundManager.playScore()
                                        addParticles(enemy.x, enemy.y, enemy.color, 15)
                                    }
                                    break
                                }
                            }

                            if (!destroyed) {
                                remainingEnemies.add(enemy)
                            }
                        }

                        tdEnemies = remainingEnemies
                        tdBullets = currentBullets
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Main Interactive Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(config.templateType) {
                    detectTapGestures { offset ->
                        when (config.templateType) {
                            GameTemplateType.RUNNER -> {
                                if (jumpCount < 2) {
                                    playerVelocityY = -16f * config.gravity
                                    isJumping = true
                                    jumpCount++
                                    SoundManager.playJump()
                                }
                            }
                            GameTemplateType.FLAPPY -> {
                                flappyVy = -12f * config.gravity
                                SoundManager.playJump()
                            }
                            GameTemplateType.SHOOTER -> {
                                lasers = lasers + Laser(shipX, shipY - 30f, vy = -20f)
                                SoundManager.playShoot()
                            }
                            GameTemplateType.MAZE -> {
                                heroMazeX = offset.x.coerceIn(50f, canvasSize.width - 50f)
                                heroMazeY = offset.y.coerceIn(100f, canvasSize.height - 100f)
                            }
                            GameTemplateType.PUZZLE -> {
                                val w = canvasSize.width
                                val h = canvasSize.height
                                val tileSize = (w - 80f) / puzzleGridSize
                                val startX = 40f
                                val startY = (h - (tileSize * puzzleGridSize)) / 2f
                                
                                val c = ((offset.x - startX) / tileSize).toInt()
                                val r = ((offset.y - startY) / tileSize).toInt()

                                if (r in 0 until puzzleGridSize && c in 0 until puzzleGridSize) {
                                    onPuzzleTileTap(r, c)
                                }
                            }
                            GameTemplateType.TOP_DOWN_SHOOTER -> {
                                tdPlayerX = offset.x.coerceIn(40f, canvasSize.width - 40f)
                                tdPlayerY = offset.y.coerceIn(150f, canvasSize.height - 150f)
                                tdBullets = tdBullets + TopDownBullet(tdPlayerX, tdPlayerY, 0f, -18f)
                                SoundManager.playShoot()
                            }
                            else -> {}
                        }
                    }
                }
                .pointerInput(config.templateType) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        when (config.templateType) {
                            GameTemplateType.BREAKOUT -> {
                                paddleX = (paddleX + dragAmount.x).coerceIn(80f, canvasSize.width - 80f)
                            }
                            GameTemplateType.SHOOTER -> {
                                shipX = (shipX + dragAmount.x).coerceIn(40f, canvasSize.width - 40f)
                                shipY = (shipY + dragAmount.y).coerceIn(200f, canvasSize.height - 100f)
                            }
                            GameTemplateType.MAZE -> {
                                heroMazeX = (heroMazeX + dragAmount.x).coerceIn(50f, canvasSize.width - 50f)
                                heroMazeY = (heroMazeY + dragAmount.y).coerceIn(100f, canvasSize.height - 100f)
                            }
                            GameTemplateType.TOP_DOWN_SHOOTER -> {
                                tdPlayerX = (tdPlayerX + dragAmount.x).coerceIn(40f, canvasSize.width - 40f)
                                tdPlayerY = (tdPlayerY + dragAmount.y).coerceIn(150f, canvasSize.height - 150f)
                            }
                            else -> {}
                        }
                    }
                }
        ) {
            canvasSize = size
            val w = size.width
            val h = size.height

            // -----------------------------------------------------------------
            // Background Visual Art Layer
            // -----------------------------------------------------------------
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF0F172A)
                    )
                )
            )

            // Dynamic Background Grid Lines
            val gridStep = 60f
            for (x in 0..(w / gridStep).toInt()) {
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(x * gridStep, 0f),
                    end = Offset(x * gridStep, h),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(h / gridStep).toInt()) {
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, y * gridStep),
                    end = Offset(w, y * gridStep),
                    strokeWidth = 1f
                )
            }

            // -----------------------------------------------------------------
            // Template-Specific Canvas Renderers
            // -----------------------------------------------------------------
            when (config.templateType) {
                GameTemplateType.RUNNER -> {
                    val groundY = h - 200f

                    // Ground Floor Line
                    drawLine(
                        color = config.primaryColor,
                        start = Offset(0f, groundY),
                        end = Offset(w, groundY),
                        strokeWidth = 6f
                    )

                    // Hero Runner Avatar
                    val heroX = 140f
                    val heroSize = 40f * config.playerSizeRatio
                    drawCircle(
                        color = config.secondaryColor,
                        center = Offset(heroX, playerY - heroSize),
                        radius = heroSize
                    )
                    drawCircle(
                        color = Color.White,
                        center = Offset(heroX + 10f, playerY - heroSize - 5f),
                        radius = 8f
                    )

                    // Obstacles
                    for (obs in runnerObstacles) {
                        if (!obs.collected) {
                            if (obs.type == 2) {
                                // Coin
                                drawCircle(
                                    color = Color(0xFFF59E0B),
                                    center = Offset(obs.x + obs.width / 2, obs.y - obs.height / 2),
                                    radius = obs.width / 2
                                )
                            } else {
                                // Spike / Barrier
                                drawRoundRect(
                                    color = obs.color,
                                    topLeft = Offset(obs.x, obs.y - obs.height),
                                    size = Size(obs.width, obs.height),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
                                )
                            }
                        }
                    }
                }

                GameTemplateType.BREAKOUT -> {
                    // Paddle
                    val pWidth = 120f * config.playerSizeRatio
                    val pHeight = 25f
                    drawRoundRect(
                        color = config.primaryColor,
                        topLeft = Offset(paddleX - pWidth / 2, h - 180f),
                        size = Size(pWidth, pHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)
                    )

                    // Bouncing Ball
                    drawCircle(
                        color = config.secondaryColor,
                        center = Offset(ballX, ballY),
                        radius = 12f
                    )

                    // Bricks
                    for (b in breakoutBricks) {
                        drawRoundRect(
                            color = b.color,
                            topLeft = b.rect.topLeft,
                            size = b.rect.size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
                        )
                    }
                }

                GameTemplateType.SHOOTER -> {
                    // Spaceship
                    val shipPath = Path().apply {
                        moveTo(shipX, shipY - 30f)
                        lineTo(shipX - 25f, shipY + 25f)
                        lineTo(shipX, shipY + 15f)
                        lineTo(shipX + 25f, shipY + 25f)
                        close()
                    }
                    drawPath(shipPath, color = config.primaryColor)

                    // Lasers
                    for (l in lasers) {
                        drawRoundRect(
                            color = config.secondaryColor,
                            topLeft = Offset(l.x - 3f, l.y - 12f),
                            size = Size(6f, 24f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
                        )
                    }

                    // Aliens
                    for (a in aliens) {
                        drawCircle(
                            color = Color(0xFFEC4899),
                            center = Offset(a.x, a.y),
                            radius = a.radius
                        )
                    }
                }

                GameTemplateType.FLAPPY -> {
                    // Bird Avatar
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        center = Offset(140f, flappyY),
                        radius = 22f
                    )
                    drawCircle(
                        color = Color.White,
                        center = Offset(152f, flappyY - 6f),
                        radius = 6f
                    )

                    // Pipes
                    for (p in flappyPipes) {
                        // Top Pipe
                        drawRoundRect(
                            color = config.primaryColor,
                            topLeft = Offset(p.x, 0f),
                            size = Size(p.width, p.gapY),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
                        )
                        // Bottom Pipe
                        drawRoundRect(
                            color = config.primaryColor,
                            topLeft = Offset(p.x, p.gapY + p.gapHeight),
                            size = Size(p.width, h - (p.gapY + p.gapHeight)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
                        )
                    }
                }

                GameTemplateType.MAZE -> {
                    // Gems
                    for (g in mazeGems) {
                        if (!g.collected) {
                            drawCircle(
                                color = Color(0xFF10B981),
                                center = Offset(g.x, g.y),
                                radius = 14f
                            )
                        }
                    }

                    // Guards
                    for (gd in mazeGuards) {
                        drawCircle(
                            color = Color(0xFFF43F5E),
                            center = Offset(gd.x, gd.y),
                            radius = 18f
                        )
                    }

                    // Player Hero Orb
                    drawCircle(
                        color = config.secondaryColor,
                        center = Offset(heroMazeX, heroMazeY),
                        radius = 20f
                    )
                }

                GameTemplateType.PUZZLE -> {
                    val tileSize = (w - 80f) / puzzleGridSize
                    val startX = 40f
                    val startY = (h - (tileSize * puzzleGridSize)) / 2f

                    for (tile in puzzleGrid) {
                        val tx = startX + tile.col * tileSize
                        val ty = startY + tile.row * tileSize

                        drawRoundRect(
                            color = puzzleColors[tile.colorIndex % puzzleColors.size],
                            topLeft = Offset(tx + 4f, ty + 4f),
                            size = Size(tileSize - 8f, tileSize - 8f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f)
                        )

                        // Inner gloss accent
                        drawCircle(
                            color = Color.White.copy(alpha = 0.3f),
                            center = Offset(tx + 20f, ty + 20f),
                            radius = 8f
                        )
                    }
                }

                GameTemplateType.TOP_DOWN_SHOOTER -> {
                    // Player Jet
                    val pPath = Path().apply {
                        moveTo(tdPlayerX, tdPlayerY - 25f)
                        lineTo(tdPlayerX - 22f, tdPlayerY + 22f)
                        lineTo(tdPlayerX, tdPlayerY + 12f)
                        lineTo(tdPlayerX + 22f, tdPlayerY + 22f)
                        close()
                    }
                    drawPath(pPath, color = config.primaryColor)

                    // Bullets
                    for (b in tdBullets) {
                        drawCircle(
                            color = config.secondaryColor,
                            center = Offset(b.x, b.y),
                            radius = 6f
                        )
                    }

                    // Enemies
                    for (e in tdEnemies) {
                        drawCircle(
                            color = e.color,
                            center = Offset(e.x, e.y),
                            radius = e.radius
                        )
                    }
                }
            }

            // Render Explosion Particles
            for (p in particles) {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    center = Offset(p.x, p.y),
                    radius = p.radius
                )
            }
        }

        // HUD Overlay (Score, Lives, Sound Mute Button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = config.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Score: ${gameState.score}",
                    color = Color(0xFF22D3EE),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    modifier = Modifier.testTag("score_board")
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        isMuted = SoundManager.toggleMute()
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Sound Mute Toggle",
                        tint = Color.White
                    )
                }

                repeat(gameState.lives) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Life",
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 4.dp)
                    )
                }
            }
        }

        // Touch Control Helper Tip (Bottom)
        if (gameState.elapsedSeconds < 4f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                val tip = when (config.templateType) {
                    GameTemplateType.RUNNER -> "Tap screen to Jump / Double Jump"
                    GameTemplateType.BREAKOUT -> "Drag finger to move Paddle"
                    GameTemplateType.SHOOTER -> "Drag to pilot Jet / Auto Firing"
                    GameTemplateType.FLAPPY -> "Tap screen to Flap & Fly"
                    GameTemplateType.MAZE -> "Drag / Tap to move Explorer Orb"
                    GameTemplateType.PUZZLE -> "Tap matching adjacent tile groups"
                    GameTemplateType.TOP_DOWN_SHOOTER -> "Drag / Tap to move & shoot enemies"
                }
                Text(text = tip, color = Color.White, fontSize = 14.sp)
            }
        }

        // Game Over / Victory Screen Modal
        AnimatedVisibility(
            visible = gameState.isGameOver,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (gameState.isVictory) "🏆 VICTORY!" else "💥 GAME OVER",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gameState.isVictory) Color(0xFF10B981) else Color(0xFFF43F5E)
                        )

                        Text(
                            text = "Final Score",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )

                        Text(
                            text = "${gameState.score}",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF22D3EE)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { resetGame() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("restart_game_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = config.primaryColor)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry")
                                Spacer(Modifier.width(8.dp))
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}
