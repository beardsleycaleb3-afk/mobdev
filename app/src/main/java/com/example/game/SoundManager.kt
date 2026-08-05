package com.example.game

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*

object SoundManager {
    private const val SAMPLE_RATE = 44100
    private var isMuted = false
    private var bgmJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        if (isMuted) {
            stopBgm()
        }
        return isMuted
    }

    fun isMuted(): Boolean = isMuted

    private fun playSynthTone(durationMs: Int, frequencyStart: Float, frequencyEnd: Float, waveType: String = "sine") {
        if (isMuted) return
        scope.launch {
            try {
                val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
                val samples = ShortArray(numSamples)
                val bufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                var currentAngle = 0.0
                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val freq = frequencyStart + (frequencyEnd - frequencyStart) * progress
                    val angleIncrement = (2.0 * Math.PI * freq) / SAMPLE_RATE
                    currentAngle += angleIncrement

                    // Fade out envelope
                    val envelope = 1.0 - progress
                    val rawSample = when (waveType) {
                        "square" -> if (Math.sin(currentAngle) > 0) 0.5 else -0.5
                        "saw" -> (2.0 * (currentAngle / (2.0 * Math.PI) - Math.floor(0.5 + currentAngle / (2.0 * Math.PI)))) * 0.4
                        else -> Math.sin(currentAngle) * 0.6
                    }

                    val sampleValue = (rawSample * envelope * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    samples[i] = sampleValue.toShort()
                }

                audioTrack.write(samples, 0, numSamples)
                audioTrack.play()

                delay(durationMs.toLong() + 50)
                audioTrack.release()
            } catch (e: Exception) {
                // Ignore audio playback exceptions if device lacks audio track
            }
        }
    }

    fun playJump() {
        playSynthTone(120, 250f, 650f, "sine")
    }

    fun playShoot() {
        playSynthTone(100, 900f, 200f, "square")
    }

    fun playScore() {
        playSynthTone(150, 600f, 1200f, "sine")
    }

    fun playCollision() {
        playSynthTone(180, 200f, 60f, "saw")
    }

    fun playGameOver() {
        playSynthTone(400, 350f, 100f, "saw")
    }

    fun playVictory() {
        scope.launch {
            playSynthTone(120, 523f, 523f, "sine")
            delay(130)
            playSynthTone(120, 659f, 659f, "sine")
            delay(130)
            playSynthTone(120, 783f, 783f, "sine")
            delay(130)
            playSynthTone(250, 1046f, 1046f, "sine")
        }
    }

    fun playPuzzlePop() {
        playSynthTone(90, 450f, 900f, "sine")
    }

    fun startBgm() {
        if (isMuted || bgmJob?.isActive == true) return
        bgmJob = scope.launch {
            val notes = listOf(261.63f, 329.63f, 392.00f, 523.25f, 392.00f, 329.63f, 293.66f, 349.23f)
            var index = 0
            while (isActive) {
                if (!isMuted) {
                    val note = notes[index % notes.size]
                    playSynthTone(150, note, note, "sine")
                    index++
                }
                delay(300)
            }
        }
    }

    fun stopBgm() {
        bgmJob?.cancel()
        bgmJob = null
    }
}
