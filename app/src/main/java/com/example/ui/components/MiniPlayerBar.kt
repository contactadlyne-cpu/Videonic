package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.PlaybackManager
import com.example.ui.theme.VideonicPrimary

@Composable
fun MiniPlayerBar(
    playbackManager: PlaybackManager,
    onExpandPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentVideo by playbackManager.currentVideo.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val currentPositionMs by playbackManager.currentPositionMs.collectAsState()
    val durationMs by playbackManager.durationMs.collectAsState()

    AnimatedVisibility(
        visible = currentVideo != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (currentVideo != null) {
            val video = currentVideo!!
            val progressFraction = if (durationMs > 0) {
                (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExpandPlayer)
                    .testTag("mini_player_bar")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Slim Progress Line
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp),
                        color = VideonicPrimary,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Video Thumbnail
                        VideoThumbnail(
                            video = video,
                            modifier = Modifier
                                .size(width = 54.dp, height = 36.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Title & Subtitle
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = video.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${com.example.ui.screens.formatTime(currentPositionMs)} / ${com.example.ui.screens.formatTime(durationMs)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Play/Pause button
                        IconButton(
                            onClick = { playbackManager.togglePlayPause() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(VideonicPrimary.copy(alpha = 0.12f))
                                .testTag("mini_player_play_pause")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = VideonicPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Stop/Close button
                        IconButton(
                            onClick = { playbackManager.stopPlayback() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("mini_player_close")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Mini Player",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
