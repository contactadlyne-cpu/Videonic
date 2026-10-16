package com.example

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.player.PlaybackManager
import com.example.ui.screens.MainApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        const val ACTION_PIP_PREV = "com.example.ACTION_PIP_PREV"
        const val ACTION_PIP_PLAY_PAUSE = "com.example.ACTION_PIP_PLAY_PAUSE"
        const val ACTION_PIP_NEXT = "com.example.ACTION_PIP_NEXT"
    }

    private lateinit var playbackManager: PlaybackManager
    private var pendingVideoUri: Uri? = null

    private val _isInPipMode = MutableStateFlow(false)
    val isInPipMode = _isInPipMode.asStateFlow()

    private var isReceiverRegistered = false

    private val pipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PIP_PREV -> playbackManager.playPreviousVideo()
                ACTION_PIP_PLAY_PAUSE -> playbackManager.togglePlayPause()
                ACTION_PIP_NEXT -> playbackManager.playNextVideo()
            }
            updatePipActions()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener { splashProvider ->
            // Just remove it immediately to show our Compose splash screen
            splashProvider.remove()
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        playbackManager = PlaybackManager(this)

        // Register PiP action broadcast receiver
        val filter = IntentFilter().apply {
            addAction(ACTION_PIP_PREV)
            addAction(ACTION_PIP_PLAY_PAUSE)
            addAction(ACTION_PIP_NEXT)
        }
        ContextCompat.registerReceiver(
            this,
            pipReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        isReceiverRegistered = true

        // Listen for playback state changes to update PiP controls live
        lifecycleScope.launch {
            playbackManager.isPlaying.collect {
                if (_isInPipMode.value) {
                    updatePipActions()
                }
            }
        }

        // Check if launched with a video intent
        handleIntent(intent)

        setContent {
            val inPip by _isInPipMode.collectAsState()
            MainApp(
                playbackManager = playbackManager,
                initialVideoUri = pendingVideoUri,
                isInPipMode = inPip,
                onEnterPiP = { enterPipMode() },
                onExitPiP = { 
                    // Optional: handle any activity level logic when exiting PiP
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
        pendingVideoUri?.let { uri ->
            playbackManager.playUri(uri, uri.lastPathSegment ?: "External Video")
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            pendingVideoUri = intent.data
        }
    }

    private fun buildPipParams(): PictureInPictureParams? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null

        val isPlaying = playbackManager.isPlaying.value
        val playPauseIconRes = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val playPauseTitle = if (isPlaying) "Pause" else "Play"

        val prevIntent = PendingIntent.getBroadcast(
            this,
            101,
            Intent(ACTION_PIP_PREV).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val playPauseIntent = PendingIntent.getBroadcast(
            this,
            102,
            Intent(ACTION_PIP_PLAY_PAUSE).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextIntent = PendingIntent.getBroadcast(
            this,
            103,
            Intent(ACTION_PIP_NEXT).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevAction = RemoteAction(
            Icon.createWithResource(this, android.R.drawable.ic_media_previous),
            "Previous",
            "Previous Video",
            prevIntent
        )
        val playPauseAction = RemoteAction(
            Icon.createWithResource(this, playPauseIconRes),
            playPauseTitle,
            playPauseTitle,
            playPauseIntent
        )
        val nextAction = RemoteAction(
            Icon.createWithResource(this, android.R.drawable.ic_media_next),
            "Next",
            "Next Video",
            nextIntent
        )

        val aspectRatio = Rational(16, 9)
        return PictureInPictureParams.Builder()
            .setAspectRatio(aspectRatio)
            .setActions(listOf(prevAction, playPauseAction, nextAction))
            .build()
    }

    private fun updatePipActions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && _isInPipMode.value) {
            buildPipParams()?.let { params ->
                setPictureInPictureParams(params)
            }
        }
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = buildPipParams() ?: PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        _isInPipMode.value = isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            updatePipActions()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // If playing video, enter PiP mode when user presses home button
        if (playbackManager.isPlaying.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPipMode()
        }
    }

    override fun onPause() {
        super.onPause()
        playbackManager.onPause(isFinishing, _isInPipMode.value)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(pipReceiver)
            } catch (e: Exception) {
                // Ignore
            }
            isReceiverRegistered = false
        }
        playbackManager.release()
    }
}
