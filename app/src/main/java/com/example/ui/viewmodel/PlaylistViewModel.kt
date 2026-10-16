package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.PlaylistEntity
import com.example.data.local.entities.PlaylistItemEntity
import com.example.data.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val videoDao = database.videoDao()

    val playlists: StateFlow<List<PlaylistEntity>> = videoDao.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val selectedPlaylist: StateFlow<PlaylistEntity?> = _selectedPlaylist.asStateFlow()

    val selectedPlaylistItems: StateFlow<List<PlaylistItemEntity>> = _selectedPlaylist.flatMapLatest { playlist ->
        if (playlist == null) flowOf(emptyList())
        else videoDao.getPlaylistItems(playlist.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectPlaylist(playlist: PlaylistEntity?) {
        _selectedPlaylist.value = playlist
    }

    fun createPlaylist(name: String, initialVideos: List<VideoItem> = emptyList()) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val playlistId = videoDao.insertPlaylist(PlaylistEntity(name = name.trim()))
            if (initialVideos.isNotEmpty()) {
                val items = initialVideos.mapIndexed { index, video ->
                    PlaylistItemEntity(
                        playlistId = playlistId,
                        videoUriString = video.uri.toString(),
                        videoTitle = video.displayName,
                        durationMs = video.durationMs,
                        sizeBytes = video.sizeBytes,
                        orderIndex = index
                    )
                }
                videoDao.insertPlaylistItems(items)
            }
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            videoDao.renamePlaylist(playlistId, newName.trim())
            _selectedPlaylist.value = _selectedPlaylist.value?.copy(name = newName.trim())
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            videoDao.deletePlaylistItems(playlistId)
            videoDao.deletePlaylist(playlistId)
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = null
            }
        }
    }

    fun addVideoToPlaylist(playlistId: Long, video: VideoItem) {
        viewModelScope.launch {
            val count = videoDao.getPlaylistItemCountSync(playlistId)
            videoDao.insertPlaylistItem(
                PlaylistItemEntity(
                    playlistId = playlistId,
                    videoUriString = video.uri.toString(),
                    videoTitle = video.displayName,
                    durationMs = video.durationMs,
                    sizeBytes = video.sizeBytes,
                    orderIndex = count
                )
            )
        }
    }

    fun addMultipleVideosToPlaylist(playlistId: Long, videos: List<VideoItem>) {
        if (videos.isEmpty()) return
        viewModelScope.launch {
            val count = videoDao.getPlaylistItemCountSync(playlistId)
            val items = videos.mapIndexed { index, video ->
                PlaylistItemEntity(
                    playlistId = playlistId,
                    videoUriString = video.uri.toString(),
                    videoTitle = video.displayName,
                    durationMs = video.durationMs,
                    sizeBytes = video.sizeBytes,
                    orderIndex = count + index
                )
            }
            videoDao.insertPlaylistItems(items)
        }
    }

    fun removeVideoFromPlaylist(playlistId: Long, videoUriString: String) {
        viewModelScope.launch {
            videoDao.removePlaylistItem(playlistId, videoUriString)
        }
    }
}
