package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiGameForgeService
import com.example.data.GameDatabase
import com.example.data.GameEntity
import com.example.data.GameRepository
import com.example.game.GameConfig
import com.example.game.GameTemplateType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameForgeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    private val geminiService = GeminiGameForgeService()

    val allGames: StateFlow<List<GameEntity>>
    val favoriteGames: StateFlow<List<GameEntity>>

    // Current Active Building Config
    private val _activeConfig = MutableStateFlow(GameConfig())
    val activeConfig: StateFlow<GameConfig> = _activeConfig.asStateFlow()

    // Screen State
    private val _isPlayingGame = MutableStateFlow(false)
    val isPlayingGame: StateFlow<Boolean> = _isPlayingGame.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Forge, 1: Arcade, 2: AI Studio, 3: Stats
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // AI Chat History Log
    private val _aiLog = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            "Hello! I'm GameForge AI Architect. Tell me any game concept (e.g., 'Cyberpunk ninja cat dodging laser mice') and I'll forge it for you!" to false
        )
    )
    val aiLog: StateFlow<List<Pair<String, Boolean>>> = _aiLog.asStateFlow()

    init {
        val dao = GameDatabase.getDatabase(application).gameDao()
        repository = GameRepository(dao)

        allGames = repository.allGames.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        favoriteGames = repository.favoriteGames.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.ensureInitialGames()
        }
    }

    fun setTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun updateConfig(config: GameConfig) {
        _activeConfig.value = config
    }

    fun launchGame(config: GameConfig = _activeConfig.value) {
        _activeConfig.value = config
        _isPlayingGame.value = true
        if (config.id > 0) {
            viewModelScope.launch {
                repository.incrementPlayCount(config.id)
            }
        }
    }

    fun exitGame() {
        _isPlayingGame.value = false
    }

    fun saveCurrentGame(onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val cfg = _activeConfig.value
            val entity = GameEntity(
                id = cfg.id,
                title = cfg.title,
                templateType = cfg.templateType.name,
                heroPrompt = cfg.heroPrompt,
                bgPrompt = cfg.bgPrompt,
                iconPrompt = cfg.iconPrompt,
                primaryColorHex = cfg.primaryColorHex,
                secondaryColorHex = cfg.secondaryColorHex,
                speedMultiplier = cfg.speedMultiplier,
                gravity = cfg.gravity,
                playerSizeRatio = cfg.playerSizeRatio
            )
            val newId = repository.saveGame(entity)
            _activeConfig.value = cfg.copy(id = newId)
            onComplete(newId)
        }
    }

    fun updateHighScore(score: Int) {
        val cfg = _activeConfig.value
        if (cfg.id > 0) {
            viewModelScope.launch {
                repository.updateHighScore(cfg.id, score)
            }
        }
    }

    fun deleteGame(entity: GameEntity) {
        viewModelScope.launch {
            repository.deleteGame(entity)
        }
    }

    fun generateWithAI(concept: String) {
        if (concept.isBlank()) return
        _isGenerating.value = true

        val currentLog = _aiLog.value.toMutableList()
        currentLog.add(concept to true)
        _aiLog.value = currentLog

        viewModelScope.launch {
            val generated = geminiService.generateGameFromPrompt(concept)
            _activeConfig.value = generated
            _isGenerating.value = false

            val updatedLog = _aiLog.value.toMutableList()
            updatedLog.add("✨ Forged '${generated.title}' (${generated.templateType.displayName})! Tap 'Launch & Play' to test it now." to false)
            _aiLog.value = updatedLog
        }
    }

    fun loadEntityToConfig(entity: GameEntity) {
        val template = try {
            GameTemplateType.valueOf(entity.templateType)
        } catch (e: Exception) {
            GameTemplateType.RUNNER
        }

        _activeConfig.value = GameConfig(
            id = entity.id,
            title = entity.title,
            templateType = template,
            heroPrompt = entity.heroPrompt,
            bgPrompt = entity.bgPrompt,
            iconPrompt = entity.iconPrompt,
            primaryColorHex = entity.primaryColorHex,
            secondaryColorHex = entity.secondaryColorHex,
            speedMultiplier = entity.speedMultiplier,
            gravity = entity.gravity,
            playerSizeRatio = entity.playerSizeRatio
        )
    }
}
