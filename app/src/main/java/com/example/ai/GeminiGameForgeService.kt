package com.example.ai

import com.example.BuildConfig
import com.example.game.GameConfig
import com.example.game.GameTemplateType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiGameForgeService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateGameFromPrompt(userConcept: String): GameConfig = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackGameGenerator(userConcept)
        }

        val promptText = """
            You are GameForge AI Engine. The user wants to create a mobile game based on this concept: "$userConcept".
            Respond with JSON ONLY in this exact format:
            {
              "title": "Short Cool Game Name",
              "templateType": "RUNNER", 
              "heroPrompt": "Description of hero character",
              "bgPrompt": "Description of background environment",
              "iconPrompt": "Description of app icon",
              "primaryColorHex": "#6366F1",
              "secondaryColorHex": "#22D3EE",
              "speedMultiplier": 1.2,
              "gravity": 1.0
            }
            Valid templateTypes are RUNNER, BREAKOUT, SHOOTER, FLAPPY, MAZE.
            Return strictly raw JSON with no markdown formatting around it.
        """.trimIndent()

        try {
            val jsonBody = JSONObject().apply {
                val partsArray = JSONArray().apply {
                    put(JSONObject().put("text", promptText))
                }
                val contentObj = JSONObject().put("parts", partsArray)
                put("contents", JSONArray().put(contentObj))
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful || responseBody.isBlank()) {
                return@withContext fallbackGameGenerator(userConcept)
            }

            val jsonResp = JSONObject(responseBody)
            val candidates = jsonResp.optJSONArray("candidates") ?: return@withContext fallbackGameGenerator(userConcept)
            val firstCand = candidates.optJSONObject(0) ?: return@withContext fallbackGameGenerator(userConcept)
            val content = firstCand.optJSONObject("content") ?: return@withContext fallbackGameGenerator(userConcept)
            val parts = content.optJSONArray("parts") ?: return@withContext fallbackGameGenerator(userConcept)
            val rawText = parts.optJSONObject(0)?.optString("text") ?: ""

            val cleanJsonStr = rawText.replace("```json", "").replace("```", "").trim()
            val parsedObj = JSONObject(cleanJsonStr)

            val title = parsedObj.optString("title", "AI Game")
            val templateStr = parsedObj.optString("templateType", "RUNNER")
            val heroPrompt = parsedObj.optString("heroPrompt", userConcept)
            val bgPrompt = parsedObj.optString("bgPrompt", "glowing world")
            val iconPrompt = parsedObj.optString("iconPrompt", "game icon")
            val primaryColorHex = parsedObj.optString("primaryColorHex", "#6366F1")
            val secondaryColorHex = parsedObj.optString("secondaryColorHex", "#22D3EE")
            val speed = parsedObj.optDouble("speedMultiplier", 1.2).toFloat()
            val gravity = parsedObj.optDouble("gravity", 1.0).toFloat()

            val template = try {
                GameTemplateType.valueOf(templateStr.uppercase())
            } catch (e: Exception) {
                GameTemplateType.RUNNER
            }

            GameConfig(
                title = if (title.isBlank()) "AI Game" else title,
                templateType = template,
                heroPrompt = heroPrompt,
                bgPrompt = bgPrompt,
                iconPrompt = iconPrompt,
                primaryColorHex = if (primaryColorHex.isBlank()) "#6366F1" else primaryColorHex,
                secondaryColorHex = if (secondaryColorHex.isBlank()) "#22D3EE" else secondaryColorHex,
                speedMultiplier = speed.coerceIn(0.5f, 2.5f),
                gravity = gravity.coerceIn(0.5f, 2.0f)
            )
        } catch (e: Exception) {
            fallbackGameGenerator(userConcept)
        }
    }

    private fun fallbackGameGenerator(userConcept: String): GameConfig {
        val lower = userConcept.lowercase()
        val template = when {
            lower.contains("break") || lower.contains("brick") -> GameTemplateType.BREAKOUT
            lower.contains("shoot") || lower.contains("space") || lower.contains("laser") -> GameTemplateType.SHOOTER
            lower.contains("flap") || lower.contains("bird") || lower.contains("fly") -> GameTemplateType.FLAPPY
            lower.contains("maze") || lower.contains("collect") -> GameTemplateType.MAZE
            else -> GameTemplateType.RUNNER
        }

        val name = if (userConcept.isNotBlank()) userConcept.take(20).replaceFirstChar { it.uppercase() } else "Pixel Quest"

        return GameConfig(
            title = name,
            templateType = template,
            heroPrompt = if (userConcept.isBlank()) "cyber hero sprite" else userConcept,
            bgPrompt = "glowing neon world",
            iconPrompt = "game forge emblem",
            primaryColorHex = "#6366F1",
            secondaryColorHex = "#22D3EE",
            speedMultiplier = 1.1f,
            gravity = 1.0f
        )
    }
}
