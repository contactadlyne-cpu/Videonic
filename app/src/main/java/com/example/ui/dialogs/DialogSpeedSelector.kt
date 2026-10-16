package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VideonicPrimary
import java.util.Locale

@Composable
fun DialogSpeedSelector(
    currentSpeed: Float,
    onDismiss: () -> Unit,
    onApplySpeed: (Float) -> Unit,
    onPreviewSpeed: (Float) -> Unit = {}
) {
    var selectedSpeed by remember { mutableFloatStateOf(currentSpeed) }
    val quickPresets = listOf(0.5f, 1.0f, 2.0f, 4.0f)

    AlertDialog(
        onDismissRequest = {
            onPreviewSpeed(currentSpeed) // revert preview
            onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = VideonicPrimary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Playback Speed",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().testTag("dialog_speed_selector")) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(VideonicPrimary.copy(alpha = 0.1f))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.2f", selectedSpeed)}x",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = VideonicPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = selectedSpeed,
                    onValueChange = {
                        selectedSpeed = it
                        onPreviewSpeed(it)
                    },
                    valueRange = 0.25f..4.0f,
                    steps = 15,
                    colors = SliderDefaults.colors(
                        thumbColor = VideonicPrimary,
                        activeTrackColor = VideonicPrimary
                    ),
                    modifier = Modifier.testTag("speed_slider")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Quick Presets",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickPresets.forEach { speed ->
                        val isSelected = kotlin.math.abs(selectedSpeed - speed) < 0.05f
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) VideonicPrimary else Color.Transparent,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedSpeed = speed
                                    onPreviewSpeed(speed)
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (speed == 1.0f) "Normal" else "${speed}x",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = {
                        selectedSpeed = 1.0f
                        onPreviewSpeed(1.0f)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("speed_reset_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VideonicPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VideonicPrimary)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESET TO 1.0X NORMAL", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApplySpeed(selectedSpeed)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = VideonicPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("speed_confirm_button").height(44.dp)
            ) {
                Text("APPLY", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onPreviewSpeed(currentSpeed) // revert
                    onDismiss()
                }
            ) {
                Text("CANCEL", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
