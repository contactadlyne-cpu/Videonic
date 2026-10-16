package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.FavoriteEntity
import com.example.data.local.entities.PlaylistEntity
import com.example.data.local.entities.PlaylistItemEntity
import com.example.data.local.entities.RecycleBinEntity
import com.example.data.local.entities.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    // --- Watch History ---
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedTimestamp DESC")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE uriString = :uriString LIMIT 1")
    suspend fun getHistoryItem(uriString: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE uriString = :uriString")
    suspend fun deleteHistoryItem(uriString: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearHistory()

    // --- Favorites ---
    @Query("SELECT * FROM favorites ORDER BY addedTimestamp DESC")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE uriString = :uriString)")
    fun isFavorite(uriString: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE uriString = :uriString)")
    suspend fun isFavoriteSync(uriString: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE uriString = :uriString")
    suspend fun removeFavorite(uriString: String)

    // --- Playlists ---
    @Query("SELECT id, name, createdTimestamp, (SELECT COUNT(*) FROM playlist_items WHERE playlistId = playlists.id) as videoCount FROM playlists ORDER BY createdTimestamp DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :newName WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, newName: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun deletePlaylistItems(playlistId: Long)

    // --- Playlist Items ---
    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getPlaylistItems(playlistId: Long): Flow<List<PlaylistItemEntity>>

    @Query("SELECT COUNT(*) FROM playlist_items WHERE playlistId = :playlistId")
    fun getPlaylistItemCount(playlistId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun getPlaylistItemCountSync(playlistId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItem(item: PlaylistItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItems(items: List<PlaylistItemEntity>)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND videoUriString = :videoUriString")
    suspend fun removePlaylistItem(playlistId: Long, videoUriString: String)

    // --- Recycle Bin ---
    @Query("SELECT * FROM recycle_bin ORDER BY deletionTimestamp DESC")
    fun getRecycleBinItems(): Flow<List<RecycleBinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecycleBinItem(item: RecycleBinEntity)

    @Query("DELETE FROM recycle_bin WHERE originalUriString = :uriString")
    suspend fun removeRecycleBinItem(uriString: String)

    @Query("SELECT * FROM recycle_bin WHERE originalUriString = :uriString LIMIT 1")
    suspend fun getRecycleBinItem(uriString: String): RecycleBinEntity?

    @Query("DELETE FROM recycle_bin")
    suspend fun clearRecycleBin()

    // --- Private Videos ---
    @Query("SELECT * FROM private_videos ORDER BY dateAdded DESC")
    fun getPrivateVideos(): Flow<List<com.example.data.local.entities.PrivateVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrivateVideo(item: com.example.data.local.entities.PrivateVideoEntity)

    @Query("DELETE FROM private_videos WHERE uriString = :uriString")
    suspend fun removePrivateVideo(uriString: String)

    @Query("SELECT EXISTS(SELECT 1 FROM private_videos WHERE uriString = :uriString)")
    suspend fun isPrivateVideo(uriString: String): Boolean
}
