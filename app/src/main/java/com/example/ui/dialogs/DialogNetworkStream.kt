package com.example.ui.dialogs

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VideonicPrimary

data class SampleStream(
    val title: String,
    val subtitle: String,
    val url: String
)

val SAMPLE_STREAMS = listOf(
    SampleStream(
        title = "Big Buck Bunny (HLS Stream)",
        subtitle = "Multi-bitrate adaptive live stream",
        url = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
    ),
    SampleStream(
        title = "Tears of Steel (MP4 1080p)",
        subtitle = "High-definition open movie stream",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
    ),
    SampleStream(
        title = "Sintel (MP4 720p)",
        subtitle = "Open source fantasy animation film",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
    )
)

@Composable
fun DialogNetworkStream(
    onDismiss: () -> Unit,
    onPlayStream: (url: String, title: String) -> Unit
) {
    var streamUrl by remember { mutableStateOf("") }
    var streamTitle by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = VideonicPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Play Network Stream",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dialog_network_stream")
            ) {
                Text(
                    text = "Enter direct HTTP, HTTPS, HLS (.m3u8), or DASH (.mpd) video link:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = streamUrl,
                    onValueChange = {
                        streamUrl = it
                        errorMessage = null
                    },
                    label = { Text("Stream URL") },
                    placeholder = { Text("https://example.com/video.m3u8") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("stream_url_input"),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Row {
                            if (streamUrl.isNotEmpty()) {
                                IconButton(onClick = { streamUrl = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                            IconButton(onClick = {
                                clipboardManager.getText()?.text?.let { clipText ->
                                    streamUrl = clipText.trim()
                                    errorMessage = null
                                }
                            }) {
                                Icon(
                                    Icons.Default.ContentPaste,
                                    contentDescription = "Paste from clipboard",
                                    tint = VideonicPrimary
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VideonicPrimary,
                        cursorColor = VideonicPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = streamTitle,
                    onValueChange = { streamTitle = it },
                    label = { Text("Stream Title (Optional)") },
                    placeholder = { Text("My Online Stream") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("stream_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VideonicPrimary,
                        cursorColor = VideonicPrimary
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Or choose a test stream:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                SAMPLE_STREAMS.forEach { sample ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                streamUrl = sample.url
                                streamTitle = sample.title
                                errorMessage = null
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = VideonicPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sample.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                )
                                Text(
                                    text = sample.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp
                                    ),
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
                    val trimmed = streamUrl.trim()
                    if (trimmed.isEmpty()) {
                        errorMessage = "Please enter or paste a valid stream URL"
                    } else if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://") && !trimmed.startsWith("rtsp://")) {
                        errorMessage = "URL must start with http://, https://, or rtsp://"
                    } else {
                        val title = if (streamTitle.trim().isNotEmpty()) {
                            streamTitle.trim()
                        } else {
                            Uri.parse(trimmed).lastPathSegment ?: "Network Stream"
                        }
                        onPlayStream(trimmed, title)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VideonicPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("play_stream_confirm_button")
            ) {
                Text("Play Stream")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
