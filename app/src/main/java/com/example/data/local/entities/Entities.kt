package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey
    val uriString: String,
    val title: String,
    val durationMs: Long,
    val positionMs: Long,
    val lastWatchedTimestamp: Long,
    val sizeBytes: Long,
    val width: Int,
    val height: Int
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val remainingMs: Long
        get() = (durationMs - positionMs).coerceAtLeast(0L)
}

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val uriString: String,
    val title: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val addedTimestamp: Long
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val videoCount: Int = 0,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_items",
    primaryKeys = ["playlistId", "videoUriString"]
)
data class PlaylistItemEntity(
    val playlistId: Long,
    val videoUriString: String,
    val videoTitle: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val orderIndex: Int
)
@Entity(tableName = "recycle_bin")
data class RecycleBinEntity(
    @PrimaryKey
    val originalUriString: String,
    val displayName: String,
    val originalPath: String,
    val recycleBinPath: String,
    val deletionTimestamp: Long,
    val sizeBytes: Long,
    val durationMs: Long,
    val mimeType: String
)

@Entity(tableName = "private_videos")
data class PrivateVideoEntity(
    @PrimaryKey
    val uriString: String,
    val title: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAdded: Long = System.currentTimeMillis(),
    val mimeType: String = "video/*",
    val width: Int = 0,
    val height: Int = 0
)
