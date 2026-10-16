package com.example.ui.sheets

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.AspectRatioMode
import com.example.player.PlaybackManager
import com.example.ui.theme.VideonicPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetPlayerOptions(
    playbackManager: PlaybackManager,
    onDismiss: () -> Unit,
    onOpenSpeed: () -> Unit,
    onOpenAspect: () -> Unit,
    onOpenAudioSub: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenEqualizer: () -> Unit = {},
    onOpenDecoder: () -> Unit = {},
    onOpenQuality: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("sheet_player_options")
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Player Options",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Playback speed
            OptionRowItem(
                icon = Icons.Default.Speed,
                title = "Playback speed",
                value = "${playbackManager.playbackSpeed.value}x",
                onClick = onOpenSpeed
            )

            // Equalizer (MX Player feature)
            OptionRowItem(
                icon = Icons.Default.Equalizer,
                title = "Equalizer",
                value = if (playbackManager.equalizerController.isEnabled.value) playbackManager.equalizerController.selectedPreset.value else "Off",
                onClick = onOpenEqualizer
            )

            // Decoder (HW / SW)
            OptionRowItem(
                icon = Icons.Default.Memory,
                title = "Decoder mode",
                value = playbackManager.decoderMode.value.label,
                onClick = onOpenDecoder
            )

            // Aspect ratio
            OptionRowItem(
                icon = Icons.Default.AspectRatio,
                title = "Aspect ratio",
                value = playbackManager.aspectRatioMode.value.label,
                onClick = onOpenAspect
            )

            // Audio track
            OptionRowItem(
                icon = Icons.Default.Audiotrack,
                title = "Audio track",
                value = "${playbackManager.audioTracks.value.size} tracks",
                onClick = onOpenAudioSub
            )

            // Subtitle track
            OptionRowItem(
                icon = Icons.Default.Subtitles,
                title = "Subtitle track",
                value = if (playbackManager.subtitleTracks.value.any { it.isSelected }) "Enabled" else "Off",
                onClick = onOpenAudioSub
            )

            // Video Quality
            OptionRowItem(
                icon = Icons.Default.Tune,
                title = "Video quality",
                value = playbackManager.videoQualityTracks.value.find { it.isSelected }?.name ?: "Auto",
                onClick = onOpenQuality
            )

            // Sleep timer
            OptionRowItem(
                icon = Icons.Default.Timer,
                title = "Sleep timer",
                value = if (playbackManager.sleepTimerSeconds.value > 0) "${playbackManager.sleepTimerSeconds.value / 60}m" else "Off",
                onClick = onOpenSleepTimer
            )

            // Repeat Mode toggle
            OptionToggleRow(
                icon = Icons.Default.Repeat,
                title = "Repeat one",
                checked = playbackManager.isRepeatOne.value,
                onCheckedChange = { playbackManager.toggleRepeatOne() }
            )

            // Video Mirror Mode (Horizontal Flip)
            OptionToggleRow(
                icon = Icons.Default.ScreenRotation,
                title = "Mirror video (Horizontal flip)",
                checked = playbackManager.isMirrored.value,
                onCheckedChange = { playbackManager.toggleMirror() }
            )

            // A-B Repeat Mode
            val isAbActive = playbackManager.isAbRepeatActive.value
            val ptA = playbackManager.repeatPointA.value
            val ptB = playbackManager.repeatPointB.value
            val abLabel = when {
                isAbActive && ptA != null && ptB != null -> "Looping (${com.example.ui.screens.formatTime(ptA)} - ${com.example.ui.screens.formatTime(ptB)})"
                ptA != null -> "Point A set (${com.example.ui.screens.formatTime(ptA)}) • Tap for B"
                else -> "Off • Tap to set Point A"
            }
            OptionRowItem(
                icon = Icons.Default.Repeat,
                title = "A-B Repeat",
                value = abLabel,
                onClick = {
                    when {
                        ptA == null -> playbackManager.setRepeatPointA()
                        !isAbActive -> playbackManager.setRepeatPointB()
                        else -> playbackManager.clearAbRepeat()
                    }
                }
            )

            // Screenshot
            val context = LocalContext.current
            OptionRowItem(
                icon = Icons.Default.CameraAlt,
                title = "Take screenshot",
                value = "",
                onClick = {
                    val curVid = playbackManager.currentVideo.value
                    com.example.ui.screens.takeVideoScreenshot(context, curVid?.uri, playbackManager.currentPositionMs.value)
                    onDismiss()
                }
            )

            // Screen lock
            OptionRowItem(
                icon = Icons.Default.Lock,
                title = "Screen lock",
                value = "",
                onClick = {
                    playbackManager.setLocked(true)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun OptionRowItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun OptionToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        com.example.ui.components.VideonicSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
