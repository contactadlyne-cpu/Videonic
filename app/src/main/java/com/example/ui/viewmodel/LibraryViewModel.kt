package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.FavoriteEntity
import com.example.data.local.entities.WatchHistoryEntity
import com.example.data.model.FolderItem
import com.example.data.model.VideoItem
import com.example.data.local.entities.RecycleBinEntity
import com.example.data.preferences.PreferencesManager
import com.example.data.preferences.VideoSortOrder
import com.example.data.preferences.ViewLayout
import com.example.data.repository.MediaRepository
import com.example.data.repository.VideoTechnicalInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import android.app.PendingIntent
import kotlinx.coroutines.delay

sealed interface LibraryUiState {
    object Loading : LibraryUiState
    data class Success(val videos: List<VideoItem>) : LibraryUiState
    data class Empty(val message: String) : LibraryUiState
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaRepository = MediaRepository(application)
    private val database = AppDatabase.getInstance(application)
    private val videoDao = database.videoDao()
    val preferencesManager = PreferencesManager(application)

    private val _rawVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    val allVideos: StateFlow<List<VideoItem>> = _rawVideos.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _hasLoadedOnce = MutableStateFlow(false)
    val hasLoadedOnce: StateFlow<Boolean> = _hasLoadedOnce.asStateFlow()

    private val _permissionRequestFlow = MutableSharedFlow<PendingIntent>()
    val permissionRequestFlow: SharedFlow<PendingIntent> = _permissionRequestFlow.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val viewLayout: StateFlow<ViewLayout> = preferencesManager.viewLayout
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ViewLayout.LIST)

    private val _sessionSortOrder = MutableStateFlow(VideoSortOrder.DATE_DESC)
    val sortOrder: StateFlow<VideoSortOrder> = _sessionSortOrder.asStateFlow()

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = videoDao.getWatchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteEntity>> = videoDao.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recycleBinItems: StateFlow<List<RecycleBinEntity>> = videoDao.getRecycleBinItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredVideos: StateFlow<List<VideoItem>> = combine(
        _rawVideos,
        _searchQuery,
        sortOrder
    ) { videos, query, sort ->
        val filtered = if (query.isBlank()) {
            videos
        } else {
            videos.filter {
                it.displayName.contains(query, ignoreCase = true) ||
                it.title.contains(query, ignoreCase = true) ||
                it.folderName.contains(query, ignoreCase = true)
            }
        }

        when (sort) {
            VideoSortOrder.DATE_DESC -> filtered.sortedByDescending { it.dateAdded }
            VideoSortOrder.DATE_ASC -> filtered.sortedBy { it.dateAdded }
            VideoSortOrder.NAME_ASC -> filtered.sortedBy { it.displayName.lowercase() }
            VideoSortOrder.NAME_DESC -> filtered.sortedByDescending { it.displayName.lowercase() }
            VideoSortOrder.SIZE_DESC -> filtered.sortedByDescending { it.sizeBytes }
            VideoSortOrder.DURATION_DESC -> filtered.sortedByDescending { it.durationMs }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<FolderItem>> = _rawVideos.combine(_searchQuery) { videos, _ ->
        mediaRepository.groupVideosByFolder(videos)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedFolder = MutableStateFlow<FolderItem?>(null)
    val selectedFolder: StateFlow<FolderItem?> = _selectedFolder.asStateFlow()

    val folderVideos: StateFlow<List<VideoItem>> = combine(_rawVideos, _selectedFolder, sortOrder) { videos, folder, sort ->
        if (folder == null) emptyList()
        else {
            val filtered = videos.filter { it.folderName.equals(folder.name, ignoreCase = true) }
            when (sort) {
                VideoSortOrder.DATE_DESC -> filtered.sortedByDescending { it.dateAdded }
                VideoSortOrder.DATE_ASC -> filtered.sortedBy { it.dateAdded }
                VideoSortOrder.NAME_ASC -> filtered.sortedBy { it.displayName.lowercase() }
                VideoSortOrder.NAME_DESC -> filtered.sortedByDescending { it.displayName.lowercase() }
                VideoSortOrder.SIZE_DESC -> filtered.sortedByDescending { it.sizeBytes }
                VideoSortOrder.DURATION_DESC -> filtered.sortedByDescending { it.durationMs }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Technical metadata inspector state
    private val _videoInfo = MutableStateFlow<VideoTechnicalInfo?>(null)
    val videoInfo: StateFlow<VideoTechnicalInfo?> = _videoInfo.asStateFlow()

    init {
        // Controlled by MainAppScaffold to avoid double loading
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedFolder.value = null
        _sessionSortOrder.value = VideoSortOrder.DATE_DESC
    }

    fun loadVideos() {
        viewModelScope.launch {
            _isLoading.value = true
            val list = mediaRepository.getDeviceVideos()
            _rawVideos.value = list
            delay(300) // Slightly longer delay for stability
            _hasLoadedOnce.value = true
            _isLoading.value = false
        }
    }

    fun addManualVideo(uri: Uri) {
        viewModelScope.launch {
            val video = mediaRepository.getVideoFromUri(uri)
            if (video != null) {
                val current = _rawVideos.value.toMutableList()
                val existingIndex = current.indexOfFirst { it.uri == uri }
                if (existingIndex >= 0) {
                    current[existingIndex] = video
                } else {
                    current.add(0, video)
                }
                _rawVideos.value = current
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun renameVideo(video: VideoItem, newName: String) {
        viewModelScope.launch {
            try {
                val success = mediaRepository.renameVideo(video, newName)
                if (success) {
                    loadVideos()
                }
            } catch (e: Exception) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && e is android.app.RecoverableSecurityException) {
                    _permissionRequestFlow.emit(e.userAction.actionIntent)
                } else {
                    e.printStackTrace()
                }
            }
        }
    }

    fun moveToRecycleBin(video: VideoItem) {
        viewModelScope.launch {
            try {
                val recycledPath = mediaRepository.moveFileToRecycleBin(video)
                if (recycledPath != null) {
                    videoDao.insertRecycleBinItem(
                        RecycleBinEntity(
                            originalUriString = video.uri.toString(),
                            displayName = video.displayName,
                            originalPath = video.path,
                            recycleBinPath = recycledPath,
                            deletionTimestamp = System.currentTimeMillis(),
                            sizeBytes = video.sizeBytes,
                            durationMs = video.durationMs,
                            mimeType = video.mimeType
                        )
                    )
                    // Remove from history/favorites if present
                    videoDao.deleteHistoryItem(video.uri.toString())
                    videoDao.removeFavorite(video.uri.toString())
                    loadVideos()
                }
            } catch (e: Exception) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && e is android.app.RecoverableSecurityException) {
                    _permissionRequestFlow.emit(e.userAction.actionIntent)
                } else {
                    e.printStackTrace()
                }
            }
        }
    }

    fun restoreFromRecycleBin(item: RecycleBinEntity) {
        viewModelScope.launch {
            val newUri = mediaRepository.restoreFile(item.recycleBinPath, item.displayName)
            if (newUri != null) {
                videoDao.removeRecycleBinItem(item.originalUriString)
                loadVideos()
            }
        }
    }

    fun permanentDelete(item: RecycleBinEntity) {
        viewModelScope.launch {
            val success = mediaRepository.deletePermanently(item.recycleBinPath)
            if (success) {
                videoDao.removeRecycleBinItem(item.originalUriString)
            }
        }
    }

    fun clearRecycleBin() {
        viewModelScope.launch {
            recycleBinItems.value.forEach {
                mediaRepository.deletePermanently(it.recycleBinPath)
            }
            videoDao.clearRecycleBin()
        }
    }

    fun setViewLayout(layout: ViewLayout) {
        viewModelScope.launch {
            preferencesManager.setViewLayout(layout)
        }
    }

    fun setSortOrder(order: VideoSortOrder) {
        _sessionSortOrder.value = order
    }

    fun selectFolder(folder: FolderItem?) {
        _selectedFolder.value = folder
    }

    fun isFavorite(uriString: String): Boolean {
        return favorites.value.any { it.uriString == uriString }
    }

    fun toggleFavorite(video: VideoItem) {
        viewModelScope.launch {
            val uriStr = video.uri.toString()
            if (isFavorite(uriStr)) {
                videoDao.removeFavorite(uriStr)
            } else {
                videoDao.addFavorite(
                    FavoriteEntity(
                        uriString = uriStr,
                        title = video.displayName,
                        durationMs = video.durationMs,
                        sizeBytes = video.sizeBytes,
                        width = video.width,
                        height = video.height,
                        addedTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun getWatchPosition(uriString: String): Long {
        return watchHistory.value.find { it.uriString == uriString }?.positionMs ?: 0L
    }

    fun saveWatchProgress(video: VideoItem, positionMs: Long) {
        viewModelScope.launch {
            videoDao.upsertHistory(
                WatchHistoryEntity(
                    uriString = video.uri.toString(),
                    title = video.displayName,
                    durationMs = video.durationMs,
                    positionMs = positionMs,
                    lastWatchedTimestamp = System.currentTimeMillis(),
                    sizeBytes = video.sizeBytes,
                    width = video.width,
                    height = video.height
                )
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            videoDao.clearHistory()
        }
    }

    fun deleteHistoryItem(uriString: String) {
        viewModelScope.launch {
            videoDao.deleteHistoryItem(uriString)
        }
    }

    fun loadVideoInfo(video: VideoItem) {
        viewModelScope.launch {
            val info = mediaRepository.extractTechnicalInfo(video.uri)
            _videoInfo.value = info
        }
    }

    fun deleteVideoPermanently(video: VideoItem) {
        viewModelScope.launch {
            try {
                getApplication<Application>().contentResolver.delete(video.uri, null, null)
                loadVideos()
            } catch (e: Exception) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && e is android.app.RecoverableSecurityException) {
                    _permissionRequestFlow.emit(e.userAction.actionIntent)
                } else {
                    e.printStackTrace()
                }
            }
        }
    }

    fun clearVideoInfo() {
        _videoInfo.value = null
    }
}
