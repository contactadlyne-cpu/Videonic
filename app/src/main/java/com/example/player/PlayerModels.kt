package com.example.player

import com.example.data.preferences.AspectRatioMode

enum class GestureType {
    NONE, VOLUME, BRIGHTNESS, SEEK, ASPECT, SPEED
}

data class GestureState(
    val type: GestureType = GestureType.NONE,
    val valueLabel: String = "",
    val progress: Float = 0f // 0f to 1f
)

data class TrackItem(
    val id: String,
    val name: String,
    val language: String?,
    val isSelected: Boolean
)

enum class DecoderMode(val label: String, val title: String, val description: String) {
    HW("HW", "Hardware Decoder", "Accelerated GPU decoding with lower battery consumption"),
    SW("SW", "Software Decoder", "CPU decoding for wider media format and codec compatibility")
}

enum class SleepTimerPreset(val minutes: Int, val label: String) {
    OFF(0, "Off"),
    M15(15, "15 minutes"),
    M30(30, "30 minutes"),
    H1(60, "1 hour"),
    H2(120, "2 hours"),
    CUSTOM(-1, "Custom time"),
    END_OF_VIDEO(-2, "End of current video")
}
