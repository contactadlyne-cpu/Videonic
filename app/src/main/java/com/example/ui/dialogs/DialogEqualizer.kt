package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.EqualizerController
import com.example.ui.theme.VideonicPrimary

@Composable
fun DialogEqualizer(
    equalizerController: EqualizerController,
    onDismiss: () -> Unit
) {
    val isEnabled by equalizerController.isEnabled.collectAsState()
    val bandLevels by equalizerController.bandLevels.collectAsState()
    val bassBoostStrength by equalizerController.bassBoostStrength.collectAsState()
    val selectedPreset by equalizerController.selectedPreset.collectAsState()

    val frequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(VideonicPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = null,
                            tint = VideonicPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Audio Equalizer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { equalizerController.setEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = VideonicPrimary
                    ),
                    modifier = Modifier.testTag("equalizer_power_switch")
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dialog_equalizer_content")
            ) {
                // Presets Horizontal Row
                Text(
                    text = "Presets (Live)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    equalizerController.presets.forEach { preset ->
                        val isSelected = selectedPreset == preset
                        FilterChip(
                            selected = isSelected,
                            enabled = isEnabled,
                            onClick = {
                                equalizerController.selectPreset(preset)
                                if (preset == "Bass Boost") {
                                    equalizerController.setBassBoost(600)
                                }
                            },
                            label = { Text(text = preset, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VideonicPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5 Graphic Equalizer Sliders
                Text(
                    text = "5-Band Graphic Equalizer (Real-Time)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 0 until 5) {
                        val levelMb = bandLevels.getOrElse(i) { 0 }
                        val levelDb = levelMb / 100 // -15dB to +15dB

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = frequencies.getOrElse(i) { "Band $i" },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(54.dp),
                                color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                            )

                            Slider(
                                value = levelMb.toFloat(),
                                onValueChange = { frac ->
                                    equalizerController.setBandLevel(i, frac.toInt())
                                },
                                valueRange = -1500f..1500f,
                                steps = 30,
                                enabled = isEnabled,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("eq_band_slider_$i"),
                                colors = SliderDefaults.colors(
                                    thumbColor = VideonicPrimary,
                                    activeTrackColor = VideonicPrimary
                                )
                            )

                            Text(
                                text = "${if (levelDb > 0) "+$levelDb" else "$levelDb"} dB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(42.dp),
                                color = if (isEnabled) VideonicPrimary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bass Boost Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speaker,
                            contentDescription = null,
                            tint = if (isEnabled) VideonicPrimary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bass Boost (Live)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                        )
                    }

                    Text(
                        text = "${(bassBoostStrength / 10)}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isEnabled) VideonicPrimary else MaterialTheme.colorScheme.outline
                    )
                }

                Slider(
                    value = bassBoostStrength.toFloat(),
                    onValueChange = { equalizerController.setBassBoost(it.toInt()) },
                    valueRange = 0f..1000f,
                    enabled = isEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = VideonicPrimary,
                        activeTrackColor = VideonicPrimary
                    ),
                    modifier = Modifier.testTag("bass_boost_slider")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = VideonicPrimary),
                modifier = Modifier.testTag("equalizer_done_button")
            ) {
                Text("DONE")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    equalizerController.resetToFlat()
                },
                modifier = Modifier.testTag("equalizer_reset_button")
            ) {
                Text("RESET")
            }
        }
    )
}
