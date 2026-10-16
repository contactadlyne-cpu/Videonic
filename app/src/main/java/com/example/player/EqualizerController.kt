package com.example.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EqualizerController {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var currentSessionId: Int = 0

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    // 5 standard audio frequency bands in Hz
    val defaultFrequencies = listOf(60, 230, 910, 3600, 14000)

    // Band levels in millibels (-1500 to +1500 mB, i.e., -15dB to +15dB)
    private val _bandLevels = MutableStateFlow(listOf(0, 0, 0, 0, 0))
    val bandLevels: StateFlow<List<Int>> = _bandLevels.asStateFlow()

    // Bass boost strength (0 to 1000)
    private val _bassBoostStrength = MutableStateFlow(250)
    val bassBoostStrength: StateFlow<Int> = _bassBoostStrength.asStateFlow()

    val presets = listOf(
        "Flat",
        "Rock",
        "Pop",
        "Jazz",
        "Classical",
        "Bass Boost",
        "Vocal",
        "Electronic",
        "Custom"
    )

    private val presetBandMap = mapOf(
        "Flat" to listOf(0, 0, 0, 0, 0),
        "Rock" to listOf(450, 250, -100, 250, 500),
        "Pop" to listOf(-150, 150, 400, 200, -100),
        "Jazz" to listOf(300, 150, -150, 200, 350),
        "Classical" to listOf(400, 250, -200, 300, 400),
        "Bass Boost" to listOf(700, 400, 100, 0, -100),
        "Vocal" to listOf(-200, 200, 500, 300, -150),
        "Electronic" to listOf(500, 300, 0, 200, 450)
    )

    private val _selectedPreset = MutableStateFlow("Flat")
    val selectedPreset: StateFlow<String> = _selectedPreset.asStateFlow()

    fun attachAudioSession(sessionId: Int) {
        if (sessionId == 0 || sessionId == currentSessionId) return
        currentSessionId = sessionId
        releaseEffects()

        try {
            equalizer = Equalizer(0, sessionId).apply {
                enabled = _isEnabled.value
            }
            bassBoost = BassBoost(0, sessionId).apply {
                enabled = _isEnabled.value
                if (strengthSupported) {
                    setStrength(_bassBoostStrength.value.toShort())
                }
            }
            applyCurrentLevelsToHardware()
        } catch (e: Exception) {
            Log.w("EqualizerController", "Hardware audio effects initialization fallback: ${e.message}")
        }
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
        } catch (e: Exception) {
            Log.w("EqualizerController", "Failed to set equalizer enabled: ${e.message}")
        }
    }

    fun setBandLevel(bandIndex: Int, levelMillibels: Int) {
        if (bandIndex !in 0 until 5) return
        val clampedLevel = levelMillibels.coerceIn(-1500, 1500)
        val updated = _bandLevels.value.toMutableList()
        updated[bandIndex] = clampedLevel
        _bandLevels.value = updated
        _selectedPreset.value = "Custom"

        try {
            equalizer?.let { eq ->
                if (bandIndex < eq.numberOfBands) {
                    eq.setBandLevel(bandIndex.toShort(), clampedLevel.toShort())
                }
            }
        } catch (e: Exception) {
            Log.w("EqualizerController", "Failed to set band level: ${e.message}")
        }
    }

    fun selectPreset(presetName: String) {
        _selectedPreset.value = presetName
        val targetLevels = presetBandMap[presetName] ?: return
        _bandLevels.value = targetLevels

        if (presetName == "Bass Boost") {
            setBassBoost(600)
        }

        applyCurrentLevelsToHardware()
    }

    fun setBassBoost(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _bassBoostStrength.value = clamped
        try {
            bassBoost?.let { bb ->
                if (bb.strengthSupported) {
                    bb.setStrength(clamped.toShort())
                }
            }
        } catch (e: Exception) {
            Log.w("EqualizerController", "Failed to set bass boost: ${e.message}")
        }
    }

    fun resetToFlat() {
        selectPreset("Flat")
        setBassBoost(200)
    }

    private fun applyCurrentLevelsToHardware() {
        try {
            equalizer?.let { eq ->
                val num = eq.numberOfBands.toInt().coerceAtMost(5)
                for (i in 0 until num) {
                    val lvl = _bandLevels.value.getOrElse(i) { 0 }.toShort()
                    eq.setBandLevel(i.toShort(), lvl)
                }
            }
        } catch (e: Exception) {
            Log.w("EqualizerController", "Failed to apply levels to hardware: ${e.message}")
        }
    }

    private fun releaseEffects() {
        try {
            equalizer?.release()
            bassBoost?.release()
        } catch (e: Exception) {
            // ignore
        } finally {
            equalizer = null
            bassBoost = null
        }
    }

    fun release() {
        releaseEffects()
        currentSessionId = 0
    }
}
