package com.example.data.model

import android.net.Uri
import java.util.Locale

data class VideoItem(
    val id: Long,
    val uri: Uri,
    val title: String,
    val displayName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val dateAdded: Long,
    val folderName: String,
    val path: String
) {
    val formattedDuration: String
        get() {
            if (durationMs <= 0) return "00:00"
            val totalSeconds = durationMs / 1000
            val seconds = totalSeconds % 60
            val minutes = (totalSeconds / 60) % 60
            val hours = totalSeconds / 3600
            return if (hours > 0) {
                String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }

    val formattedSize: String
        get() {
            if (sizeBytes <= 0) return "0 B"
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
                mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
                kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
                else -> "$sizeBytes B"
            }
        }

    val resolutionLabel: String
        get() {
            val maxDim = maxOf(width, height)
            val minDim = minOf(width, height)
            return when {
                maxDim >= 3840 || minDim >= 2160 -> "4K"
                maxDim >= 2560 || minDim >= 1440 -> "2K"
                maxDim >= 1920 || minDim >= 1080 -> "1080p"
                maxDim >= 1280 || minDim >= 720 -> "720p"
                maxDim >= 854 || minDim >= 480 -> "480p"
                maxDim > 0 -> "${width}x${height}"
                else -> "HD"
            }
        }

    val formatExtension: String
        get() {
            val dot = displayName.lastIndexOf('.')
            return if (dot != -1 && dot < displayName.length - 1) {
                displayName.substring(dot + 1).uppercase(Locale.US)
            } else {
                mimeType.substringAfter('/', "MP4").uppercase(Locale.US)
            }
        }
}

data class FolderItem(
    val name: String,
    val path: String,
    val videoCount: Int,
    val representativeUri: Uri?
)
