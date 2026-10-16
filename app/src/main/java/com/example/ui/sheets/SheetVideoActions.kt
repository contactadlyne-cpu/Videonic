package com.example.ui.sheets

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.ui.components.VideoThumbnail
import com.example.ui.theme.FavoriteRed
import com.example.ui.theme.VideonicPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetVideoActions(
    video: VideoItem,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onMoveToPrivate: () -> Unit = {},
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onInfo: () -> Unit
) {
    val context = LocalContext.current
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
                .padding(bottom = 28.dp)
                .testTag("sheet_video_actions")
        ) {
            // Video Preview Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VideoThumbnail(
                    video = video,
                    modifier = Modifier.size(width = 80.dp, height = 54.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.displayName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${video.formattedDuration} • ${video.formattedSize} • ${video.resolutionLabel}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            ActionItemRow(
                icon = Icons.Default.PlayArrow,
                title = "Play",
                onClick = {
                    onDismiss()
                    onPlay()
                }
            )

            ActionItemRow(
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                title = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                iconTint = if (isFavorite) FavoriteRed else MaterialTheme.colorScheme.primary,
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                }
            )

            ActionItemRow(
                icon = Icons.Default.PlaylistAdd,
                title = "Add to Playlist",
                onClick = {
                    onDismiss()
                    onAddToPlaylist()
                }
            )

            ActionItemRow(
                icon = Icons.Default.Edit,
                title = "Rename",
                onClick = {
                    onDismiss()
                    onRename()
                }
            )

            ActionItemRow(
                icon = Icons.Default.Lock,
                title = "Move to Private Vault",
                iconTint = VideonicPrimary,
                onClick = {
                    onDismiss()
                    onMoveToPrivate()
                }
            )

            ActionItemRow(
                icon = Icons.Default.Delete,
                title = "Delete",
                iconTint = MaterialTheme.colorScheme.error,
                onClick = {
                    onDismiss()
                    onDelete()
                }
            )

            ActionItemRow(
                icon = Icons.Default.Share,
                title = "Share",
                onClick = {
                    onDismiss()
                    shareVideo(context, video)
                }
            )

            ActionItemRow(
                icon = Icons.Default.Info,
                title = "File Information",
                onClick = {
                    onDismiss()
                    onInfo()
                }
            )
        }
    }
}

@Composable
fun ActionItemRow(
    icon: ImageVector,
    title: String,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun shareVideo(context: Context, video: VideoItem) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = video.mimeType.ifEmpty { "video/*" }
        putExtra(Intent.EXTRA_STREAM, video.uri)
        putExtra(Intent.EXTRA_SUBJECT, video.displayName)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Video"))
}
