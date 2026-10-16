package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entities.FavoriteEntity
import com.example.data.local.entities.WatchHistoryEntity
import com.example.data.model.FolderItem
import com.example.data.model.VideoItem
import com.example.data.preferences.ThemeMode
import com.example.player.PlaybackManager
import com.example.player.SleepTimerPreset
import com.example.ui.components.HomeTab
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.VideonicTabs
import com.example.ui.components.VideonicTopBar
import com.example.ui.dialogs.DialogAddToPlaylist
import com.example.ui.dialogs.DialogDecoderSelector
import com.example.ui.dialogs.DialogEqualizer
import com.example.ui.dialogs.DialogNetworkStream
import com.example.ui.dialogs.DialogRename
import com.example.ui.dialogs.DialogSleepTimer
import com.example.ui.dialogs.DialogThemeSelection
import com.example.ui.dialogs.DialogVideoQualitySelector
import com.example.ui.sheets.SheetAudio
import com.example.ui.sheets.SheetSubtitles
import com.example.ui.sheets.SheetMoreMenu
import com.example.ui.sheets.SheetPlayerOptions
import com.example.ui.sheets.SheetVideoActions
import com.example.ui.sheets.SheetVideoInfo
import com.example.ui.theme.VideonicLiteTheme
import com.example.ui.theme.VideonicPrimary
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.PlaylistViewModel
import com.example.ui.viewmodel.SettingsViewModel

enum class CurrentScreen {
    SPLASH,
    PERMISSION_PRIMER,
    HOME,
    FOLDER_DETAILS,
    SEARCH,
    PLAYER,
    PLAYLISTS,
    PLAYLIST_DETAILS,
    FAVORITES,
    SETTINGS,
    ABOUT,
    REPORT,
    PRIVACY_POLICY,
    LICENSES,
    RECYCLE_BIN,
    PRIVATE_FOLDER
}

enum class BottomNavTab(val label: String, val icon: ImageVector) {
    VIDEOS("Videos", Icons.Default.VideoLibrary),
    FOLDERS("Folders", Icons.Default.Folder),
    RECENT("Recent", Icons.Default.History),
    ME("Me", Icons.Default.Person)
}

@Composable
fun MainApp(
    playbackManager: PlaybackManager,
    initialVideoUri: Uri? = null,
    isInPipMode: Boolean = false,
    onEnterPiP: () -> Unit = {},
    onExitPiP: () -> Unit = {}
) {
    val context = LocalContext.current
    val libraryViewModel: LibraryViewModel = viewModel()
    val playlistViewModel: PlaylistViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    val themeMode by settingsViewModel.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemDark
    }

    val showThumbnails by settingsViewModel.showThumbnails.collectAsState()
    val resumePlayback by settingsViewModel.resumePlayback.collectAsState()
    val autoPlayNext by settingsViewModel.autoPlayNext.collectAsState()
    val defaultSpeed by settingsViewModel.defaultSpeed.collectAsState()
    val keepScreenOn by settingsViewModel.keepScreenOn.collectAsState()

    LaunchedEffect(Unit) {
        playbackManager.libraryVideosSupplier = { libraryViewModel.filteredVideos.value }
    }

    LaunchedEffect(autoPlayNext) {
        playbackManager.setAutoPlayNext(autoPlayNext)
    }
    LaunchedEffect(defaultSpeed) {
        playbackManager.setSpeed(defaultSpeed)
    }

    // When in Android Picture-in-Picture mode, only render the playing video surface
    if (isInPipMode) {
        val aspectMode by playbackManager.aspectRatioMode.collectAsState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            VideoPlayerSurface(
                playbackManager = playbackManager,
                aspectRatioMode = aspectMode,
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    // Permission check upfront
    val hasVideoPermission = remember {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(hasVideoPermission) {
        if (hasVideoPermission) {
            libraryViewModel.loadVideos()
        }
    }

    // Single splash screen: native system splash transitions directly to Home or Permission primer
    var currentScreen by rememberSaveable {
        mutableStateOf(if (hasVideoPermission) CurrentScreen.HOME else CurrentScreen.PERMISSION_PRIMER)
    }
    var selectedBottomTab by remember { mutableStateOf(BottomNavTab.VIDEOS) }

    // Always reset orientation to Portrait when not on video player screen
    LaunchedEffect(currentScreen) {
        if (currentScreen != CurrentScreen.PLAYER) {
            (context as? Activity)?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    // Bottom Sheets & Dialogs State
    var showPlayerOptionsSheet by remember { mutableStateOf(false) }
    var showAudioSubSheet by remember { mutableStateOf(false) }
    var showSubtitleSubSheet by remember { mutableStateOf(false) }
    var audioSubSheetTab by remember { mutableIntStateOf(0) }
    var showVideoInfoSheet by remember { mutableStateOf(false) }
    var showVideoActionsSheet by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showDecoderDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var videoToDelete by remember { mutableStateOf<VideoItem?>(null) }
    var selectedVideoForAction by remember { mutableStateOf<VideoItem?>(null) }
    var autoOpenAddVideosForPlaylist by remember { mutableStateOf(false) }

    // Intent Sender Launcher for Scoped Storage permissions (Rename/Delete)
    val intentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            libraryViewModel.loadVideos()
        }
    }

    LaunchedEffect(Unit) {
        libraryViewModel.permissionRequestFlow.collect { pendingIntent ->
            intentSenderLauncher.launch(
                androidx.activity.result.IntentSenderRequest.Builder(pendingIntent.intentSender).build()
            )
        }
    }
    LaunchedEffect(isInPipMode) {
        if (!isInPipMode && playbackManager.currentVideo.value != null) {
            onExitPiP()
            currentScreen = CurrentScreen.PLAYER
        }
    }

    // Periodic watch progress save while playing
    LaunchedEffect(currentScreen) {
        while (currentScreen == CurrentScreen.PLAYER) {
            delay(3000)
            val cur = playbackManager.currentVideo.value
            val pos = playbackManager.currentPositionMs.value
            if (cur != null && pos > 0) {
                libraryViewModel.saveWatchProgress(cur, pos)
            }
        }
    }

    // SAF File Picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            libraryViewModel.addManualVideo(uri)
            playbackManager.playUri(uri, uri.lastPathSegment ?: "Selected Video")
            currentScreen = CurrentScreen.PLAYER
        }
    }

    // Handle initial intent URI if opened from another app
    LaunchedEffect(initialVideoUri) {
        if (initialVideoUri != null) {
            playbackManager.playUri(initialVideoUri, initialVideoUri.lastPathSegment ?: "External Video")
            currentScreen = CurrentScreen.PLAYER
        }
    }

    VideonicLiteTheme(darkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                CurrentScreen.SPLASH -> {
                    ScreenSplash(
                        onSplashFinished = {
                            currentScreen = if (initialVideoUri != null || playbackManager.currentVideo.value != null) {
                                CurrentScreen.PLAYER
                            } else if (hasVideoPermission) {
                                CurrentScreen.HOME
                            } else {
                                CurrentScreen.PERMISSION_PRIMER
                            }
                        }
                    )
                }

                CurrentScreen.PERMISSION_PRIMER -> {
                    ScreenPermission(
                        onPermissionGranted = {
                            settingsViewModel.setHasSeenPermissionPrimer(true)
                            libraryViewModel.loadVideos()
                            currentScreen = CurrentScreen.HOME
                        },
                        onChooseManually = {
                            settingsViewModel.setHasSeenPermissionPrimer(true)
                            videoPickerLauncher.launch(arrayOf("video/*"))
                        }
                    )
                }

                CurrentScreen.HOME, CurrentScreen.FOLDER_DETAILS, CurrentScreen.FAVORITES, CurrentScreen.PLAYLISTS, CurrentScreen.RECYCLE_BIN, CurrentScreen.PRIVATE_FOLDER, CurrentScreen.SETTINGS, CurrentScreen.ABOUT, CurrentScreen.REPORT -> {
                    BackHandler {
                        if (currentScreen != CurrentScreen.HOME) {
                            currentScreen = CurrentScreen.HOME
                        } else if (selectedBottomTab != BottomNavTab.VIDEOS) {
                            selectedBottomTab = BottomNavTab.VIDEOS
                        } else {
                            val now = System.currentTimeMillis()
                            if (now - lastBackPressTime < 2000L) {
                                (context as? Activity)?.finish()
                            } else {
                                lastBackPressTime = now
                                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    val videos by libraryViewModel.filteredVideos.collectAsState()
                    val allVideosList by libraryViewModel.allVideos.collectAsState()
                    val isLoading by libraryViewModel.isLoading.collectAsState()
                    val hasLoadedOnce by libraryViewModel.hasLoadedOnce.collectAsState()
                    val viewLayout by libraryViewModel.viewLayout.collectAsState()
                    val sortOrder by libraryViewModel.sortOrder.collectAsState()
                    val folders by libraryViewModel.folders.collectAsState()
                    val recentList by libraryViewModel.watchHistory.collectAsState()
                    val favoritesList by libraryViewModel.favorites.collectAsState()
                    val playlistsList by playlistViewModel.playlists.collectAsState()
                    val privateVideosList by libraryViewModel.privateVideos.collectAsState()

                    val launchVideoWithResumeCheck: (VideoItem, List<VideoItem>) -> Unit = { video, playlist ->
                        val savedPos = libraryViewModel.getWatchPosition(video.uri.toString())
                        val shouldResume = resumePlayback && savedPos > 3000L && (video.durationMs <= 0 || savedPos < video.durationMs - 5000L)
                        val startPos = if (shouldResume) savedPos else 0L
                        playbackManager.playVideo(video, startPos, playlist)
                        currentScreen = CurrentScreen.PLAYER
                    }

                    Scaffold(
                        topBar = {
                            if (currentScreen != CurrentScreen.FOLDER_DETAILS) {
                                val title = when (currentScreen) {
                                    CurrentScreen.FAVORITES -> "Favorites"
                                    CurrentScreen.PLAYLISTS -> "Playlists"
                                    CurrentScreen.RECYCLE_BIN -> "Recycle Bin"
                                    CurrentScreen.SETTINGS -> "Settings"
                                    CurrentScreen.ABOUT -> "About"
                                    CurrentScreen.REPORT -> "Report & Feedback"
                                    else -> when (selectedBottomTab) {
                                        BottomNavTab.VIDEOS -> "Videonic Lite"
                                        BottomNavTab.FOLDERS -> "Folders"
                                        BottomNavTab.RECENT -> "Recent"
                                        BottomNavTab.ME -> ""
                                    }
                                }
                                VideonicTopBar(
                                    title = title,
                                    showActions = currentScreen == CurrentScreen.HOME && selectedBottomTab != BottomNavTab.ME,
                                    viewLayout = viewLayout,
                                    onSearchClick = { currentScreen = CurrentScreen.SEARCH },
                                    onRefreshClick = { libraryViewModel.loadVideos() },
                                    onViewLayoutChange = { libraryViewModel.setViewLayout(it) },
                                    onSortOrderChange = { libraryViewModel.setSortOrder(it) }
                                )
                            }
                        },
                        bottomBar = {
                            Column {
                                val curPlayingVideo by playbackManager.currentVideo.collectAsState()
                                if (curPlayingVideo != null && currentScreen != CurrentScreen.PLAYER) {
                                    MiniPlayerBar(
                                        playbackManager = playbackManager,
                                        onExpandPlayer = { currentScreen = CurrentScreen.PLAYER }
                                    )
                                }
                                if (currentScreen == CurrentScreen.HOME) {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 3.dp,
                                        modifier = Modifier.testTag("bottom_navigation_bar")
                                    ) {
                                        BottomNavTab.values().forEach { tab ->
                                            val isSelected = selectedBottomTab == tab && currentScreen == CurrentScreen.HOME
                                            NavigationBarItem(
                                                selected = isSelected,
                                                onClick = {
                                                    selectedBottomTab = tab
                                                    currentScreen = CurrentScreen.HOME
                                                },
                                                icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                                                label = {
                                                    Text(
                                                        text = tab.label,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 11.sp
                                                    )
                                                },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = Color.White,
                                                    selectedTextColor = VideonicPrimary,
                                                    indicatorColor = VideonicPrimary,
                                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                modifier = Modifier.testTag("bottom_nav_${tab.name.lowercase()}")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentScreen) {
                                CurrentScreen.HOME -> {
                                    when (selectedBottomTab) {
                                        BottomNavTab.VIDEOS -> {
                                                ScreenHomeVideos(
                                                videos = videos,
                                                isLoading = isLoading,
                                                viewLayout = viewLayout,
                                                sortOrder = sortOrder,
                                                showThumbnails = showThumbnails,
                                                watchHistory = recentList,
                                                onViewLayoutChange = { libraryViewModel.setViewLayout(it) },
                                                onSortOrderChange = { libraryViewModel.setSortOrder(it) },
                                                onVideoClick = { video ->
                                                    launchVideoWithResumeCheck(video, videos)
                                                },
                                                onVideoOptionsClick = { video ->
                                                    selectedVideoForAction = video
                                                    showVideoActionsSheet = true
                                                },
                                                onPickManualVideo = { videoPickerLauncher.launch(arrayOf("video/*")) },
                                                hasLoadedOnce = hasLoadedOnce
                                            )
                                        }

                                        BottomNavTab.FOLDERS -> {
                                            ScreenFolders(
                                                folders = folders,
                                                isLoading = isLoading,
                                                onFolderClick = { folder ->
                                                    libraryViewModel.selectFolder(folder)
                                                    currentScreen = CurrentScreen.FOLDER_DETAILS
                                                }
                                            )
                                        }

                                        BottomNavTab.RECENT -> {
                                            ScreenRecentVideos(
                                                historyList = recentList,
                                                sortOrder = sortOrder,
                                                onPlayHistory = { history ->
                                                    val video = videos.find { it.uri.toString() == history.uriString }
                                                    if (video != null) {
                                                        playbackManager.playVideo(video, history.positionMs, videos)
                                                    } else {
                                                        playbackManager.playUri(Uri.parse(history.uriString), history.title)
                                                    }
                                                    currentScreen = CurrentScreen.PLAYER
                                                },
                                                onDeleteHistoryItem = { uriStr ->
                                                    libraryViewModel.deleteHistoryItem(uriStr)
                                                },
                                                onClearHistory = {
                                                    libraryViewModel.clearHistory()
                                                }
                                            )
                                        }

                                        BottomNavTab.ME -> {
                                            ScreenMe(
                                                videos = allVideosList,
                                                favoritesCount = favoritesList.size,
                                                playlistsCount = playlistsList.size,
                                                privateVideosCount = privateVideosList.size,
                                                onOpenFavorites = { currentScreen = CurrentScreen.FAVORITES },
                                                onOpenPlaylists = { currentScreen = CurrentScreen.PLAYLISTS },
                                                onOpenPrivateFolder = { currentScreen = CurrentScreen.PRIVATE_FOLDER },
                                                onOpenRecycleBin = { currentScreen = CurrentScreen.RECYCLE_BIN },
                                                onOpenSettings = { currentScreen = CurrentScreen.SETTINGS },
                                                onOpenThemeDialog = { showThemeDialog = true },
                                                onOpenAbout = { currentScreen = CurrentScreen.ABOUT },
                                                onOpenReport = { currentScreen = CurrentScreen.REPORT }
                                            )
                                        }
                                    }
                                }
                                CurrentScreen.FOLDER_DETAILS -> {
                                    val selectedFolder by libraryViewModel.selectedFolder.collectAsState()
                                    val folderVideos by libraryViewModel.folderVideos.collectAsState()
                                    val viewLayout by libraryViewModel.viewLayout.collectAsState()
                                    val sortOrder by libraryViewModel.sortOrder.collectAsState()

                                    if (selectedFolder != null) {
                                        ScreenFolderDetails(
                                            folder = selectedFolder!!,
                                            videos = folderVideos,
                                            viewLayout = viewLayout,
                                            sortOrder = sortOrder,
                                            onBackClick = { currentScreen = CurrentScreen.HOME },
                                            onViewLayoutChange = { libraryViewModel.setViewLayout(it) },
                                            onSortOrderChange = { libraryViewModel.setSortOrder(it) },
                                            onVideoClick = { video ->
                                                val savedPos = libraryViewModel.getWatchPosition(video.uri.toString())
                                                val shouldResume = resumePlayback && savedPos > 3000L && (video.durationMs <= 0 || savedPos < video.durationMs - 5000L)
                                                val startPos = if (shouldResume) savedPos else 0L
                                                playbackManager.playVideo(video, startPos, folderVideos)
                                                currentScreen = CurrentScreen.PLAYER
                                            },
                                            onVideoOptionsClick = { video ->
                                                selectedVideoForAction = video
                                                showVideoActionsSheet = true
                                            }
                                        )
                                    }
                                }
                                CurrentScreen.FAVORITES -> {
                                    ScreenFavorites(
                                        favorites = favoritesList,
                                        onPlayFavorite = { fav ->
                                            val vid = allVideosList.find { it.uri.toString() == fav.uriString }
                                            if (vid != null) {
                                                playbackManager.playVideo(vid, 0L, allVideosList)
                                            } else {
                                                playbackManager.playUri(Uri.parse(fav.uriString), fav.title)
                                            }
                                            currentScreen = CurrentScreen.PLAYER
                                        },
                                        onRemoveFavorite = { fav ->
                                            libraryViewModel.removeFavorite(fav.uriString)
                                        }
                                    )
                                }

                                CurrentScreen.PLAYLISTS -> {
                                    ScreenPlaylists(
                                        playlists = playlistsList,
                                        onBackClick = { currentScreen = CurrentScreen.HOME },
                                        onPlaylistClick = { playlist ->
                                            playlistViewModel.selectPlaylist(playlist)
                                            currentScreen = CurrentScreen.PLAYLIST_DETAILS
                                        },
                                        onCreatePlaylistClick = { showCreatePlaylistDialog = true }
                                    )
                                }

                                CurrentScreen.RECYCLE_BIN -> {
                                    ScreenRecycleBin(
                                        viewModel = libraryViewModel,
                                        onBackClick = { currentScreen = CurrentScreen.HOME }
                                    )
                                }

                                CurrentScreen.SETTINGS -> {
                                    ScreenSettings(
                                        viewModel = settingsViewModel,
                                        onBackClick = { currentScreen = CurrentScreen.HOME },
                                        onOpenThemeDialog = { showThemeDialog = true },
                                        onRefreshLibrary = { libraryViewModel.loadVideos() },
                                        onClearHistory = { libraryViewModel.clearHistory() },
                                        onAboutClick = { currentScreen = CurrentScreen.ABOUT }
                                    )
                                }

                                CurrentScreen.ABOUT -> {
                                    ScreenAbout(onBackClick = { currentScreen = CurrentScreen.HOME })
                                }

                                CurrentScreen.REPORT -> {
                                    ScreenReport(onBackClick = { currentScreen = CurrentScreen.HOME })
                                }

                                CurrentScreen.PRIVATE_FOLDER -> {
                                    ScreenPrivateFolder(
                                        libraryViewModel = libraryViewModel,
                                        onBackClick = { currentScreen = CurrentScreen.HOME },
                                        onPlayPrivateVideo = { privVideo ->
                                            playbackManager.playUri(Uri.parse(privVideo.uriString), privVideo.title)
                                            currentScreen = CurrentScreen.PLAYER
                                        }
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }


                CurrentScreen.SEARCH -> {
                    BackHandler { currentScreen = CurrentScreen.HOME }
                    val query by libraryViewModel.searchQuery.collectAsState()
                    val filtered by libraryViewModel.filteredVideos.collectAsState()
                    val curPlayingVideo by playbackManager.currentVideo.collectAsState()

                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            ScreenSearch(
                                query = query,
                                onQueryChange = { libraryViewModel.setSearchQuery(it) },
                                results = filtered,
                                onBackClick = {
                                    libraryViewModel.setSearchQuery("")
                                    currentScreen = CurrentScreen.HOME
                                },
                                onVideoClick = { video ->
                                    val savedPos = libraryViewModel.getWatchPosition(video.uri.toString())
                                    val shouldResume = resumePlayback && savedPos > 3000L && (video.durationMs <= 0 || savedPos < video.durationMs - 5000L)
                                    val startPos = if (shouldResume) savedPos else 0L
                                    playbackManager.playVideo(video, startPos, filtered)
                                    currentScreen = CurrentScreen.PLAYER
                                },
                                onVideoOptionsClick = { video ->
                                    selectedVideoForAction = video
                                    showVideoActionsSheet = true
                                }
                            )
                        }
                        if (curPlayingVideo != null) {
                            MiniPlayerBar(
                                playbackManager = playbackManager,
                                onExpandPlayer = { currentScreen = CurrentScreen.PLAYER }
                            )
                        }
                    }
                }

                CurrentScreen.PLAYER -> {
                    BackHandler {
                        val curVideo = playbackManager.currentVideo.value
                        if (curVideo != null) {
                            libraryViewModel.saveWatchProgress(curVideo, playbackManager.currentPositionMs.value)
                        }
                        playbackManager.pausePlayback()
                        currentScreen = CurrentScreen.HOME
                    }

                    ScreenVideoPlayer(
                        playbackManager = playbackManager,
                        autoRotateSetting = settingsViewModel.autoRotateVideo.collectAsState().value,
                        keepScreenOnSetting = keepScreenOn,
                        onBackClick = {
                            val curVideo = playbackManager.currentVideo.value
                            if (curVideo != null) {
                                libraryViewModel.saveWatchProgress(curVideo, playbackManager.currentPositionMs.value)
                            }
                            playbackManager.pausePlayback()
                            currentScreen = CurrentScreen.HOME
                        },
                        onOptionsClick = { showPlayerOptionsSheet = true },
                        onAudioClick = {
                            showAudioSubSheet = true
                            audioSubSheetTab = 0
                        },
                        onSubtitleClick = {
                            showSubtitleSubSheet = true
                            audioSubSheetTab = 1
                        },
                        onSpeedClick = { showPlayerOptionsSheet = true },
                        onAspectClick = {
                            playbackManager.cycleAspectRatio()
                        },
                        onPlaylistClick = { currentScreen = CurrentScreen.PLAYLISTS },
                        onPiPClick = onEnterPiP
                    )
                }

                CurrentScreen.PLAYLIST_DETAILS -> {
                    BackHandler { currentScreen = CurrentScreen.PLAYLISTS }
                    val selectedPlaylist by playlistViewModel.selectedPlaylist.collectAsState()
                    val playlistItems by playlistViewModel.selectedPlaylistItems.collectAsState()
                    val allVideos by libraryViewModel.allVideos.collectAsState()
                    val curPlayingVideo by playbackManager.currentVideo.collectAsState()

                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (selectedPlaylist != null) {
                                ScreenPlaylistDetails(
                                    playlist = selectedPlaylist!!,
                                    items = playlistItems,
                                    allVideos = allVideos,
                                    autoOpenAddVideos = autoOpenAddVideosForPlaylist,
                                    onBackClick = {
                                        autoOpenAddVideosForPlaylist = false
                                        currentScreen = CurrentScreen.PLAYLISTS
                                    },
                                    onPlayItem = { item ->
                                        val playlistVideos = playlistItems.mapNotNull { pi -> allVideos.find { it.uri.toString() == pi.videoUriString } }
                                        val vid = allVideos.find { it.uri.toString() == item.videoUriString }
                                        if (vid != null) {
                                            playbackManager.playVideo(vid, 0L, if (playlistVideos.isNotEmpty()) playlistVideos else listOf(vid))
                                        } else {
                                            playbackManager.playUri(Uri.parse(item.videoUriString), item.videoTitle)
                                        }
                                        currentScreen = CurrentScreen.PLAYER
                                    },
                                    onPlayAll = {
                                        if (playlistItems.isNotEmpty()) {
                                            val playlistVideos = playlistItems.mapNotNull { pi -> allVideos.find { it.uri.toString() == pi.videoUriString } }
                                            val first = playlistVideos.firstOrNull() ?: allVideos.find { it.uri.toString() == playlistItems.first().videoUriString }
                                            if (first != null) {
                                                playbackManager.playVideo(first, 0L, playlistVideos)
                                            } else {
                                                playbackManager.playUri(Uri.parse(playlistItems.first().videoUriString), playlistItems.first().videoTitle)
                                            }
                                            currentScreen = CurrentScreen.PLAYER
                                        }
                                    },
                                    onShuffle = {
                                        if (playlistItems.isNotEmpty()) {
                                            val playlistVideos = playlistItems.mapNotNull { pi -> allVideos.find { it.uri.toString() == pi.videoUriString } }.shuffled()
                                            val first = playlistVideos.firstOrNull() ?: allVideos.find { it.uri.toString() == playlistItems.first().videoUriString }
                                            if (first != null) {
                                                playbackManager.playVideo(first, 0L, playlistVideos)
                                            } else {
                                                playbackManager.playUri(Uri.parse(playlistItems.first().videoUriString), playlistItems.first().videoTitle)
                                            }
                                            currentScreen = CurrentScreen.PLAYER
                                        }
                                    },
                                    onAddVideos = { videosToAdd ->
                                        playlistViewModel.addMultipleVideosToPlaylist(selectedPlaylist!!.id, videosToAdd)
                                    },
                                    onRemoveItem = { item ->
                                        playlistViewModel.removeVideoFromPlaylist(selectedPlaylist!!.id, item.videoUriString)
                                    },
                                    onDeletePlaylist = {
                                        playlistViewModel.deletePlaylist(selectedPlaylist!!.id)
                                        currentScreen = CurrentScreen.PLAYLISTS
                                    }
                                )
                            }
                        }
                        if (curPlayingVideo != null) {
                            MiniPlayerBar(
                                playbackManager = playbackManager,
                                onExpandPlayer = { currentScreen = CurrentScreen.PLAYER }
                            )
                        }
                    }
                }

                else -> {}
            }

            // Bottom Sheets
            if (showPlayerOptionsSheet) {
                SheetPlayerOptions(
                    playbackManager = playbackManager,
                    onDismiss = { showPlayerOptionsSheet = false },
                    onOpenSpeed = {
                        val nextSpeed = when (playbackManager.playbackSpeed.value) {
                            1.0f -> 1.25f
                            1.25f -> 1.5f
                            1.5f -> 2.0f
                            2.0f -> 0.5f
                            0.5f -> 0.75f
                            else -> 1.0f
                        }
                        playbackManager.setSpeed(nextSpeed)
                    },
                    onOpenAspect = {
                        val nextAspect = when (playbackManager.aspectRatioMode.value) {
                            com.example.data.preferences.AspectRatioMode.FIT -> com.example.data.preferences.AspectRatioMode.FILL
                            com.example.data.preferences.AspectRatioMode.FILL -> com.example.data.preferences.AspectRatioMode.ZOOM
                            com.example.data.preferences.AspectRatioMode.ZOOM -> com.example.data.preferences.AspectRatioMode.SIXTEEN_NINE
                            com.example.data.preferences.AspectRatioMode.SIXTEEN_NINE -> com.example.data.preferences.AspectRatioMode.FOUR_THREE
                            else -> com.example.data.preferences.AspectRatioMode.FIT
                        }
                        playbackManager.setAspectRatio(nextAspect)
                    },
                    onOpenAudioSub = {
                        showPlayerOptionsSheet = false
                        showAudioSubSheet = true
                    },
                    onOpenSleepTimer = {
                        showPlayerOptionsSheet = false
                        showSleepTimerDialog = true
                    },
                    onOpenEqualizer = {
                        showPlayerOptionsSheet = false
                        showEqualizerDialog = true
                    },
                    onOpenDecoder = {
                        showPlayerOptionsSheet = false
                        showDecoderDialog = true
                    },
                    onOpenQuality = {
                        showPlayerOptionsSheet = false
                        showQualityDialog = true
                    }
                )
            }

            if (showAudioSubSheet) {
                SheetAudio(
                    playbackManager = playbackManager,
                    onDismiss = { showAudioSubSheet = false }
                )
            }

            if (showSubtitleSubSheet) {
                SheetSubtitles(
                    playbackManager = playbackManager,
                    onDismiss = { showSubtitleSubSheet = false }
                )
            }

            if (showVideoActionsSheet && selectedVideoForAction != null) {
                val video = selectedVideoForAction!!
                val isFav = libraryViewModel.isFavorite(video.uri.toString())
                SheetVideoActions(
                    video = video,
                    isFavorite = isFav,
                    onDismiss = { showVideoActionsSheet = false },
                    onPlay = {
                        showVideoActionsSheet = false
                        val savedPos = libraryViewModel.getWatchPosition(video.uri.toString())
                        val shouldResume = resumePlayback && savedPos > 3000L && (video.durationMs <= 0 || savedPos < video.durationMs - 5000L)
                        val startPos = if (shouldResume) savedPos else 0L
                        playbackManager.playVideo(video, startPos, libraryViewModel.filteredVideos.value)
                        currentScreen = CurrentScreen.PLAYER
                    },
                    onToggleFavorite = {
                        libraryViewModel.toggleFavorite(video)
                    },
                    onAddToPlaylist = {
                        showAddToPlaylistDialog = true
                    },
                    onMoveToPrivate = {
                        libraryViewModel.moveToPrivateFolder(video)
                        Toast.makeText(context, "Moved to Private Vault", Toast.LENGTH_SHORT).show()
                    },
                    onRename = {
                        showRenameDialog = true
                    },
                    onDelete = {
                        videoToDelete = video
                        showDeleteConfirmDialog = true
                    },
                    onInfo = {
                        libraryViewModel.loadVideoInfo(video)
                        showVideoInfoSheet = true
                    }
                )
            }

            if (showRenameDialog && selectedVideoForAction != null) {
                com.example.ui.dialogs.DialogRename(
                    initialName = selectedVideoForAction!!.displayName,
                    onDismiss = { showRenameDialog = false },
                    onConfirm = { newName ->
                        libraryViewModel.renameVideo(selectedVideoForAction!!, newName)
                    }
                )
            }

            // Delete / Recycle Bin Popup
            if (showDeleteConfirmDialog && videoToDelete != null) {
                val target = videoToDelete!!
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("Delete Video") },
                    text = { Text("Do you want to move \"${target.displayName}\" to the Recycle Bin or delete it permanently?") },
                    confirmButton = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    libraryViewModel.moveToRecycleBin(target)
                                    showDeleteConfirmDialog = false
                                    videoToDelete = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = VideonicPrimary)
                            ) {
                                Text("Move to Recycle Bin")
                            }
                            Button(
                                onClick = {
                                    libraryViewModel.deleteVideoPermanently(target)
                                    showDeleteConfirmDialog = false
                                    videoToDelete = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Permanent Delete")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showVideoInfoSheet && selectedVideoForAction != null) {
                val info by libraryViewModel.videoInfo.collectAsState()
                SheetVideoInfo(
                    video = selectedVideoForAction!!,
                    info = info,
                    onDismiss = {
                        showVideoInfoSheet = false
                        libraryViewModel.clearVideoInfo()
                    }
                )
            }


            if (showAddToPlaylistDialog && selectedVideoForAction != null) {
                val playlists by playlistViewModel.playlists.collectAsState()
                DialogAddToPlaylist(
                    playlists = playlists,
                    onDismiss = { showAddToPlaylistDialog = false },
                    onPlaylistSelected = { playlist ->
                        playlistViewModel.addVideoToPlaylist(playlist.id, selectedVideoForAction!!)
                        showAddToPlaylistDialog = false
                    },
                    onCreateNewPlaylist = {
                        showAddToPlaylistDialog = false
                        showCreatePlaylistDialog = true
                    }
                )
            }

            if (showCreatePlaylistDialog) {
                DialogCreatePlaylist(
                    onDismiss = { showCreatePlaylistDialog = false },
                    onConfirm = { name ->
                        val initial = selectedVideoForAction?.let { listOf(it) } ?: emptyList()
                        playlistViewModel.createPlaylist(name, initial) { created ->
                            playlistViewModel.selectPlaylist(created)
                            autoOpenAddVideosForPlaylist = initial.isEmpty()
                            currentScreen = CurrentScreen.PLAYLIST_DETAILS
                        }
                    }
                )
            }

            if (showSleepTimerDialog) {
                DialogSleepTimer(
                    currentPreset = SleepTimerPreset.OFF,
                    onDismiss = { showSleepTimerDialog = false },
                    onSelectPreset = { preset ->
                        playbackManager.setSleepTimer(preset)
                    }
                )
            }

            if (showThemeDialog) {
                DialogThemeSelection(
                    currentTheme = themeMode,
                    onDismiss = { showThemeDialog = false },
                    onSelectTheme = { mode ->
                        settingsViewModel.setThemeMode(mode)
                    }
                )
            }

            if (showEqualizerDialog) {
                DialogEqualizer(
                    equalizerController = playbackManager.equalizerController,
                    onDismiss = { showEqualizerDialog = false }
                )
            }

            if (showDecoderDialog) {
                DialogDecoderSelector(
                    currentMode = playbackManager.decoderMode.collectAsState().value,
                    onDismiss = { showDecoderDialog = false },
                    onApplyDecoder = { mode -> playbackManager.setDecoderMode(mode) }
                )
            }

            if (showQualityDialog) {
                val qualityTracks by playbackManager.videoQualityTracks.collectAsState()
                DialogVideoQualitySelector(
                    qualityTracks = qualityTracks,
                    onDismiss = { showQualityDialog = false },
                    onSelectQuality = { track -> playbackManager.selectVideoQualityTrack(track) }
                )
            }


        }
    }
}
