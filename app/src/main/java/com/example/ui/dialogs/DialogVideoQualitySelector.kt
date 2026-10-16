package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.TrackItem
import com.example.ui.theme.VideonicPrimary

@Composable
fun DialogVideoQualitySelector(
    qualityTracks: List<TrackItem>,
    onDismiss: () -> Unit,
    onSelectQuality: (TrackItem?) -> Unit
) {
    val initialSelection = qualityTracks.find { it.isSelected }
    var selectedTrack by remember { mutableStateOf<TrackItem?>(initialSelection) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HighQuality,
                    contentDescription = null,
                    tint = VideonicPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Video Quality",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().testTag("dialog_quality_selector")) {
                // Auto option
                val isAutoSelected = selectedTrack == null
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTrack = null }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isAutoSelected,
                        onClick = { selectedTrack = null },
                        colors = RadioButtonDefaults.colors(selectedColor = VideonicPrimary)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto (Recommended)",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isAutoSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Dynamically adjusts for smoothest playback",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                qualityTracks.forEach { track ->
                    val isSelected = selectedTrack?.id == track.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTrack = track }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedTrack = track },
                            colors = RadioButtonDefaults.colors(selectedColor = VideonicPrimary)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!track.language.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = track.language,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSelectQuality(selectedTrack)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = VideonicPrimary),
                modifier = Modifier.testTag("quality_apply_button")
            ) {
                Text("APPLY")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
