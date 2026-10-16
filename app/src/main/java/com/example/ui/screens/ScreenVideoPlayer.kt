package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.preferences.AspectRatioMode
import com.example.player.DecoderMode
import com.example.player.GestureState
import com.example.player.GestureType
import com.example.player.PlaybackManager
import com.example.ui.dialogs.DialogDecoderSelector
import com.example.ui.dialogs.DialogEqualizer
import com.example.ui.dialogs.DialogSpeedSelector
import com.example.ui.dialogs.DialogVideoQualitySelector
import com.example.ui.theme.VideonicPrimary
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs
import kotlin.math.hypot

private enum class PlayerDragMode {
    NONE,
    BRIGHTNESS,
    VOLUME,
    SEEK
}

@OptIn(UnstableApi::class)
@Composable
fun ScreenVideoPlayer(
    playbackManager: PlaybackManager,
    autoRotateSetting: Boolean = false,
    keepScreenOnSetting: Boolean = true,
    onBackClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onAudioClick: () -> Unit,
    onSubtitleClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onAspectClick: () -> Unit,
    onPlaylistClick: () -> Unit,
    onPiPClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val currentVideo by playbackManager.currentVideo.collectAsState()
    val isPlaying by playbackManager.isPlaying.collectAsState()
    val currentPositionMs by playbackManager.currentPositionMs.collectAsState()
    val durationMs by playbackManager.durationMs.collectAsState()
    val isLocked by playbackManager.isLocked.collectAsState()
    val aspectRatioMode by playbackManager.aspectRatioMode.collectAsState()
    val playbackSpeed by playbackManager.playbackSpeed.collectAsState()
    val isSpeedBoostActive by playbackManager.isSpeedBoostActive.collectAsState()
    val decoderMode by playbackManager.decoderMode.collectAsState()
    val isEnded by playbackManager.isEnded.collectAsState()
    val autoResumedFromMs by playbackManager.autoResumedFromMs.collectAsState()
    // Background audio removed as per user request

    var areControlsVisible by remember { mutableStateOf(true) }
    var gestureState by remember { mutableStateOf(GestureState()) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderPositionMs by remember { mutableLongStateOf(0L) }

    var showSpeedDialog by remember { mutableStateOf(false) }
    var showDecoderDialog by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }

    // Manage Activity Orientation based on setting
    LaunchedEffect(autoRotateSetting) {
        activity?.requestedOrientation = if (autoRotateSetting) ActivityInfo.SCREEN_ORIENTATION_SENSOR else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    // Make controls visible immediately when video ends so Replay icon is displayed
    LaunchedEffect(isEnded) {
        if (isEnded) {
            areControlsVisible = true
        }
    }

    // Auto-dismiss auto-resume banner after 6 seconds
    LaunchedEffect(autoResumedFromMs) {
        if (autoResumedFromMs != null) {
            delay(6000)
            playbackManager.clearAutoResumeBanner()
        }
    }

    // Screen brightness state (0.01f to 1.0f)
    var currentBrightness by remember {
        mutableFloatStateOf(
            activity?.window?.attributes?.screenBrightness?.takeIf { it >= 0 } ?: 0.5f
        )
    }
    var activeLeftControlMode by remember { mutableStateOf(GestureType.BRIGHTNESS) }

    // Mirroring, A-B Repeat and Pinch Zoom states
    val isMirrored by playbackManager.isMirrored.collectAsState()
    val isAbRepeatActive by playbackManager.isAbRepeatActive.collectAsState()
    val repeatPointA by playbackManager.repeatPointA.collectAsState()
    val repeatPointB by playbackManager.repeatPointB.collectAsState()
    var zoomScale by remember { mutableFloatStateOf(1f) }

    // Auto-hide controls timer (4s)
    LaunchedEffect(areControlsVisible, isPlaying, isDraggingSlider, isSpeedBoostActive, showSpeedDialog, showDecoderDialog, showEqualizerDialog, showQualityDialog) {
        if (areControlsVisible && isPlaying && !isDraggingSlider && !isSpeedBoostActive && !showSpeedDialog && !showDecoderDialog && !showEqualizerDialog && !showQualityDialog) {
            delay(4000)
            areControlsVisible = false
        }
    }

    // Reveal controls when video ends
    LaunchedEffect(isEnded) {
        if (isEnded) {
            areControlsVisible = true
        }
    }

    // Auto-dismiss gesture HUD
    LaunchedEffect(gestureState.type, gestureState.valueLabel) {
        if (gestureState.type != GestureType.NONE) {
            delay(1200)
            gestureState = GestureState()
        }
    }

    // Keep screen on during playback & restore portrait orientation when leaving player
    DisposableEffect(keepScreenOnSetting) {
        if (keepScreenOnSetting) {
            activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("video_player_screen")
            .background(Color.Black)
            .pointerInput(isLocked) {
                if (isLocked) return@pointerInput

                var lastTapTime = 0L
                var lastTapX = 0f
                var initialPinchSpan = 0f
                var baseZoomScale = 1f

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downTime = System.currentTimeMillis()
                    val downPos = down.position
                    val isLeft = downPos.x < size.width / 2

                    var isSpeedBoostTriggered = false
                    var dragMode = PlayerDragMode.NONE
                    var startVolume = playbackManager.getCurrentVolume()
                    val maxVolume = playbackManager.getMaxVolume()
                    var startBrightness = currentBrightness
                    var scrubTargetMs = playbackManager.exoPlayer.currentPosition
                    var currentGestureSpeed = 2.0f
                    initialPinchSpan = 0f

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressedChanges = event.changes.filter { it.pressed }

                        // 2-Finger Pinch to Zoom Detection
                        if (pressedChanges.size >= 2) {
                            val p0 = pressedChanges[0].position
                            val p1 = pressedChanges[1].position
                            val span = hypot((p0.x - p1.x).toDouble(), (p0.y - p1.y).toDouble()).toFloat()
                            if (initialPinchSpan == 0f) {
                                initialPinchSpan = span
                                baseZoomScale = zoomScale
                            } else if (initialPinchSpan > 0f) {
                                val newScale = (baseZoomScale * (span / initialPinchSpan)).coerceIn(1.0f, 3.5f)
                                zoomScale = newScale
                                gestureState = GestureState(
                                    type = GestureType.ASPECT,
                                    valueLabel = "Zoom ${(newScale * 100).toInt()}%",
                                    progress = (newScale - 1f) / 2.5f
                                )
                            }
                            pressedChanges.forEach { it.consume() }
                            continue
                        }

                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (!change.pressed) {
                            // Finger lifted!
                            break
                        }

                        val dx = change.position.x - downPos.x
                        val dy = change.position.y - downPos.y
                        val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()
                        val elapsed = System.currentTimeMillis() - downTime

                        // 1. Detect directional swipe or long-press
                        if (dragMode == PlayerDragMode.NONE && !isSpeedBoostTriggered) {
                            if (distance > 16f) {
                                // Direction Lock
                                if (abs(dy) > abs(dx)) {
                                    // Vertical swipe gesture
                                    // Left side adjusts Brightness or Volume based on activeLeftControlMode (default Brightness)
                                    // Right side adjusts Volume
                                    if (downPos.x < size.width * 0.5f) {
                                        if (activeLeftControlMode == GestureType.VOLUME) {
                                            dragMode = PlayerDragMode.VOLUME
                                            startVolume = playbackManager.getCurrentVolume()
                                        } else {
                                            dragMode = PlayerDragMode.BRIGHTNESS
                                            startBrightness = currentBrightness
                                        }
                                    } else {
                                        dragMode = PlayerDragMode.VOLUME
                                        startVolume = playbackManager.getCurrentVolume()
                                    }
                                } else {
                                    dragMode = PlayerDragMode.SEEK
                                    scrubTargetMs = playbackManager.exoPlayer.currentPosition
                                }
                            } else if (elapsed > 450L && distance < 18f) {
                                // Press & Hold for 2x Speed Boost (only when held stationary)
                                isSpeedBoostTriggered = true
                                currentGestureSpeed = if (playbackSpeed < 1.25f) 2.0f else playbackSpeed
                                playbackManager.setTemporarySpeedBoost(true, currentGestureSpeed)
                            }
                        }

                        // 2. Perform smooth drag based on locked mode
                        if (isSpeedBoostTriggered) {
                            change.consume()
                            // Horizontal slide: Right to increase, Left to decrease
                            val sensitivity = 0.01f
                            currentGestureSpeed = (2.0f + (dx * sensitivity)).coerceIn(0.25f, 4.0f)
                            playbackManager.setTemporarySpeedBoost(true, currentGestureSpeed)
                            gestureState = GestureState(
                                type = GestureType.SPEED,
                                valueLabel = "${String.format(Locale.US, "%.2f", currentGestureSpeed)}x Speed",
                                progress = (currentGestureSpeed - 0.25f) / 3.75f
                            )
                        } else {
                            when (dragMode) {
                                PlayerDragMode.BRIGHTNESS -> {
                                    change.consume()
                                    val delta = -dy / (size.height * 0.55f)
                                    val newBrightness = (startBrightness + delta).coerceIn(0.01f, 1f)
                                    currentBrightness = newBrightness
                                    activity?.window?.let { win ->
                                        val lp = win.attributes
                                        lp.screenBrightness = newBrightness
                                        win.attributes = lp
                                    }
                                    gestureState = GestureState(
                                        type = GestureType.BRIGHTNESS,
                                        valueLabel = "${(newBrightness * 100).toInt()}%",
                                        progress = newBrightness
                                    )
                                }

                                PlayerDragMode.VOLUME -> {
                                    change.consume()
                                    val delta = -dy / (size.height * 0.55f)
                                    val volChange = (delta * maxVolume).toInt()
                                    val newVol = (startVolume + volChange).coerceIn(0, maxVolume)
                                    playbackManager.setVolume(newVol)
                                    gestureState = GestureState(
                                        type = GestureType.VOLUME,
                                        valueLabel = "$newVol / $maxVolume",
                                        progress = newVol.toFloat() / maxVolume.toFloat().coerceAtLeast(1f)
                                    )
                                }

                                PlayerDragMode.SEEK -> {
                                    change.consume()
                                    val seekDeltaSec = (dx / 10).toInt()
                                    val target = (playbackManager.exoPlayer.currentPosition + seekDeltaSec * 1000L)
                                        .coerceIn(0L, durationMs.coerceAtLeast(0L))
                                    scrubTargetMs = target
                                    gestureState = GestureState(
                                        type = GestureType.SEEK,
                                        valueLabel = "${if (seekDeltaSec >= 0) "+" else ""}${seekDeltaSec}s (${formatTime(target)})",
                                        progress = if (durationMs > 0) target.toFloat() / durationMs.toFloat() else 0f
                                    )
                                }

                                PlayerDragMode.NONE -> {}
                            }
                        }
                    }

                    // On finger release: auto-select and leave video at current gesture speed
                    if (isSpeedBoostTriggered) {
                        playbackManager.setSpeed(currentGestureSpeed)
                        playbackManager.setTemporarySpeedBoost(false)
                        gestureState = GestureState(
                            type = GestureType.SPEED,
                            valueLabel = "${String.format(Locale.US, "%.2f", currentGestureSpeed)}x Speed Selected",
                            progress = (currentGestureSpeed - 0.25f) / 3.75f
                        )
                    }

                    if (dragMode == PlayerDragMode.SEEK) {
                        playbackManager.seekTo(scrubTargetMs)
                    } else if (dragMode == PlayerDragMode.NONE && !isSpeedBoostTriggered) {
                        // Check for Double-tap vs Single-tap
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < 320L && hypot((downPos.x - lastTapX).toDouble(), 0.0) < 120) {
                            // Double tap
                            if (zoomScale > 1.05f) {
                                zoomScale = 1f
                            } else if (downPos.x < size.width / 2) {
                                playbackManager.seekRelative(-10000)
                                gestureState = GestureState(GestureType.SEEK, "-10s", 0f)
                            } else {
                                playbackManager.seekRelative(10000)
                                gestureState = GestureState(GestureType.SEEK, "+10s", 1f)
                            }
                            lastTapTime = 0L
                        } else {
                            // Single tap
                            lastTapTime = now
                            lastTapX = downPos.x
                            areControlsVisible = !areControlsVisible
                        }
                    }

                    dragMode = PlayerDragMode.NONE
                }
            }
    ) {
        // ExoPlayer View with Pinch-to-Zoom & Horizontal Video Mirror
        VideoPlayerSurface(
            playbackManager = playbackManager,
            aspectRatioMode = aspectRatioMode,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = (if (isMirrored) -1f else 1f) * zoomScale
                    scaleY = zoomScale
                }
        )

        // Reset Zoom badge if zoomed in
        if (zoomScale > 1.05f) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xCC000000),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp)
                    .clickable { zoomScale = 1f }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Zoom ${(zoomScale * 100).toInt()}% • Reset",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Active A-B Repeat Loop Banner
        if (isAbRepeatActive && repeatPointA != null && repeatPointB != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xDD0F172A),
                border = BorderStroke(1.dp, VideonicPrimary),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 70.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        tint = VideonicPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "A-B Loop: ${formatTime(repeatPointA!!)} - ${formatTime(repeatPointB!!)}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { playbackManager.clearAbRepeat() },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear A-B Repeat",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Small Compact Upper Center Speed Selection Popup
        val availableSpeeds = remember { listOf(0.5f, 1.0f, 2.0f, 4.0f) }

        if (isSpeedBoostActive || showSpeedDialog) {
            val activeSpeed = if (isSpeedBoostActive) playbackManager.exoPlayer.playbackParameters.speed else playbackSpeed
            
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xF00F172A),
                border = BorderStroke(1.dp, VideonicPrimary.copy(alpha = 0.5f)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
                    .width(320.dp)
                    .testTag("upper_center_speed_popup")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.25x", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text("4.0x", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                    Slider(
                        value = activeSpeed.coerceIn(0.25f, 4.0f),
                        onValueChange = { playbackManager.setSpeed(it) },
                        valueRange = 0.25f..4.0f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = VideonicPrimary,
                            activeTrackColor = VideonicPrimary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.2f", activeSpeed)}x Speed Playing",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        // Vertical Swipe Gesture Control on Left Side (Screen Brightness & Volume)
        val isVerticalGestureActive = gestureState.type == GestureType.BRIGHTNESS || gestureState.type == GestureType.VOLUME
        val showLeftVerticalControl = (isVerticalGestureActive || areControlsVisible) && !isLocked

        if (showLeftVerticalControl) {
            val displayType = if (isVerticalGestureActive) gestureState.type else activeLeftControlMode
            val currentVolume = playbackManager.getCurrentVolume()
            val maxVolume = playbackManager.getMaxVolume().coerceAtLeast(1)
            val displayProgress = if (displayType == GestureType.BRIGHTNESS) {
                currentBrightness
            } else {
                currentVolume.toFloat() / maxVolume.toFloat()
            }
            val displayLabel = if (displayType == GestureType.BRIGHTNESS) {
                "${(currentBrightness * 100).toInt()}%"
            } else {
                "$currentVolume / $maxVolume"
            }

            VerticalSwipeGestureControl(
                type = displayType,
                progress = displayProgress,
                valueLabel = displayLabel,
                isInteractive = isVerticalGestureActive,
                onModeToggle = {
                    activeLeftControlMode = if (activeLeftControlMode == GestureType.BRIGHTNESS) {
                        GestureType.VOLUME
                    } else {
                        GestureType.BRIGHTNESS
                    }
                },
                onProgressChange = { newProgress ->
                    if (displayType == GestureType.BRIGHTNESS) {
                        val newBrightness = newProgress.coerceIn(0.01f, 1f)
                        currentBrightness = newBrightness
                        activity?.window?.let { win ->
                            val lp = win.attributes
                            lp.screenBrightness = newBrightness
                            win.attributes = lp
                        }
                        gestureState = GestureState(
                            type = GestureType.BRIGHTNESS,
                            valueLabel = "${(newBrightness * 100).toInt()}%",
                            progress = newBrightness
                        )
                    } else {
                        val newVol = (newProgress * maxVolume).toInt().coerceIn(0, maxVolume)
                        playbackManager.setVolume(newVol)
                        gestureState = GestureState(
                            type = GestureType.VOLUME,
                            valueLabel = "$newVol / $maxVolume",
                            progress = newVol.toFloat() / maxVolume.toFloat().coerceAtLeast(1f)
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp)
            )
        }

        // Center Gesture HUD Overlay (Seek, Aspect Ratio, Speed)
        if (gestureState.type == GestureType.SEEK || gestureState.type == GestureType.ASPECT || gestureState.type == GestureType.SPEED) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = when (gestureState.type) {
                            GestureType.SEEK -> Icons.Default.Forward10
                            GestureType.ASPECT -> Icons.Default.AspectRatio
                            GestureType.SPEED -> Icons.Default.Speed
                            else -> Icons.Default.PlayArrow
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = gestureState.valueLabel,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Locked mode overlay: floating Unlock icon
        if (isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = { playbackManager.setLocked(false) },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000))
                        .testTag("unlock_screen_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Unlock Controls",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Full Controls Overlay
        AnimatedVisibility(
            visible = areControlsVisible && !isLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x99000000),
                                Color.Transparent,
                                Color(0xCC000000)
                            )
                        )
                    )
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = currentVideo?.displayName ?: "Videonic Player",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )

                    // MX Player: Decoder Mode Toggle Badge (HW/SW)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0x66000000),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .clickable { showDecoderDialog = true }
                            .padding(end = 4.dp)
                            .testTag("player_decoder_badge")
                    ) {
                        Text(
                            text = decoderMode.label,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Background Audio Playback Toggle (Headphones 🎧)
                    val isBgAudioOn by playbackManager.isBackgroundAudioEnabled.collectAsState()
                    IconButton(
                        onClick = {
                            val enabled = playbackManager.toggleBackgroundAudio()
                            android.widget.Toast.makeText(
                                context,
                                if (enabled) "Background Audio Playback Enabled" else "Background Audio Playback Disabled",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.testTag("player_headphones_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "Background Audio Playback",
                            tint = if (isBgAudioOn) com.example.ui.theme.VideonicPrimary else Color.White
                        )
                    }

                    // Audio Track / Voice Language Selection
                    IconButton(
                        onClick = onAudioClick,
                        modifier = Modifier.testTag("player_audio_track_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = "Audio Track Selection",
                            tint = Color.White
                        )
                    }

                    // Subtitle Toggle
                    IconButton(
                        onClick = onSubtitleClick,
                        modifier = Modifier.testTag("player_subtitle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = "Subtitle Selection",
                            tint = Color.White
                        )
                    }



                    // PiP Button
                    IconButton(
                        onClick = onPiPClick,
                        modifier = Modifier.testTag("player_pip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPictureAlt,
                            contentDescription = "Picture-in-Picture",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            val current = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            val next = when (current) {
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
                                else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                            activity?.requestedOrientation = next
                            
                            val label = when (next) {
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> "Locked Portrait"
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> "Locked Landscape"
                                else -> "Auto Orientation"
                            }
                            gestureState = GestureState(type = GestureType.SPEED, valueLabel = label)
                        },
                        modifier = Modifier.testTag("player_fullscreen_button")
                    ) {
                        val icon = if (activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                            Icons.Default.FullscreenExit
                        } else {
                            Icons.Default.Fullscreen
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Toggle Orientation",
                            tint = Color.White
                        )
                    }
                }

                // Center Playback Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Video
                    val prevInteractionSource = remember { MutableInteractionSource() }
                    val isPrevPressed by prevInteractionSource.collectIsPressedAsState()
                    val prevScale by animateFloatAsState(if (isPrevPressed) 0.85f else 1f, label = "prev_scale")

                    IconButton(
                        onClick = { playbackManager.playPreviousVideo() },
                        interactionSource = prevInteractionSource,
                        modifier = Modifier
                            .size(44.dp)
                            .scale(prevScale)
                            .clip(CircleShape)
                            .background(Color(0x55000000))
                            .testTag("prev_video_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Video",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Rewind 10s
                    val rewindInteractionSource = remember { MutableInteractionSource() }
                    val isRewindPressed by rewindInteractionSource.collectIsPressedAsState()
                    val rewindScale by animateFloatAsState(if (isRewindPressed) 0.85f else 1f, label = "rewind_scale")

                    IconButton(
                        onClick = { playbackManager.seekRelative(-10000) },
                        interactionSource = rewindInteractionSource,
                        modifier = Modifier
                            .size(48.dp)
                            .scale(rewindScale)
                            .clip(CircleShape)
                            .background(Color(0x55000000))
                            .testTag("rewind_10s_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10 Seconds",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Play / Pause / Restart (Requirement 25)
                    val playInteractionSource = remember { MutableInteractionSource() }
                    val isPlayPressed by playInteractionSource.collectIsPressedAsState()
                    val playScale by animateFloatAsState(if (isPlayPressed) 0.9f else 1f, label = "play_scale")
                    
                    IconButton(
                        onClick = { playbackManager.togglePlayPause() },
                        interactionSource = playInteractionSource,
                        modifier = Modifier
                            .size(68.dp)
                            .scale(playScale)
                            .clip(CircleShape)
                            .background(VideonicPrimary)
                            .testTag("play_pause_button")
                    ) {
                        Icon(
                            imageVector = when {
                                isEnded -> Icons.Default.Replay
                                isPlaying -> Icons.Default.Pause
                                else -> Icons.Default.PlayArrow
                            },
                            contentDescription = when {
                                isEnded -> "Restart Video"
                                isPlaying -> "Pause"
                                else -> "Play"
                            },
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Forward 10s
                    val forwardInteractionSource = remember { MutableInteractionSource() }
                    val isForwardPressed by forwardInteractionSource.collectIsPressedAsState()
                    val forwardScale by animateFloatAsState(if (isForwardPressed) 0.85f else 1f, label = "forward_scale")

                    IconButton(
                        onClick = { playbackManager.seekRelative(10000) },
                        interactionSource = forwardInteractionSource,
                        modifier = Modifier
                            .size(48.dp)
                            .scale(forwardScale)
                            .clip(CircleShape)
                            .background(Color(0x55000000))
                            .testTag("forward_10s_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10 Seconds",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Next Video
                    val nextInteractionSource = remember { MutableInteractionSource() }
                    val isNextPressed by nextInteractionSource.collectIsPressedAsState()
                    val nextScale by animateFloatAsState(if (isNextPressed) 0.85f else 1f, label = "next_scale")

                    IconButton(
                        onClick = { playbackManager.playNextVideo() },
                        interactionSource = nextInteractionSource,
                        modifier = Modifier
                            .size(44.dp)
                            .scale(nextScale)
                            .clip(CircleShape)
                            .background(Color(0x55000000))
                            .testTag("next_video_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Video",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Bottom Timeline and Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Time and Seekbar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayPos = if (isDraggingSlider) sliderPositionMs else currentPositionMs
                        Text(
                            text = formatTime(displayPos),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        val progress = if (durationMs > 0) {
                            (displayPos.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        Slider(
                            value = progress,
                            onValueChange = { frac ->
                                isDraggingSlider = true
                                sliderPositionMs = (frac * durationMs).toLong()
                            },
                            onValueChangeFinished = {
                                playbackManager.seekTo(sliderPositionMs)
                                isDraggingSlider = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .testTag("player_seekbar"),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White, // More visible thumb
                                activeTrackColor = VideonicPrimary,
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )

                        Text(
                            text = formatTime(durationMs),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Secondary Quick-Action Control Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Lock
                        QuickActionButton(
                            icon = Icons.Default.Lock,
                            label = "Lock",
                            onClick = { playbackManager.setLocked(true) }
                        )

                        // Speed: Pops up speed dialog first with instant preview and confirmation (Requirement 18 & 19)
                        QuickActionButton(
                            icon = Icons.Default.Speed,
                            label = "${playbackSpeed}x",
                            onClick = { showSpeedDialog = true }
                        )

                        // Aspect Ratio: Cycles aspect ratio directly with HUD feedback!
                        QuickActionButton(
                            icon = Icons.Default.AspectRatio,
                            label = aspectRatioMode.label,
                            onClick = {
                                val nextMode = playbackManager.cycleAspectRatio()
                                gestureState = GestureState(
                                    type = GestureType.ASPECT,
                                    valueLabel = "Aspect: ${nextMode.label}",
                                    progress = 1f
                                )
                            }
                        )

                        // Audio Equalizer (MX Player feature)
                        QuickActionButton(
                            icon = Icons.Default.Equalizer,
                            label = "EQ",
                            onClick = { showEqualizerDialog = true }
                        )

                        // Video Quality Selector
                        val qualityTracks by playbackManager.videoQualityTracks.collectAsState()
                        QuickActionButton(
                            icon = Icons.Default.Tune,
                            label = "Quality",
                            onClick = { showQualityDialog = true }
                        )
                    }
                }
            }
        }

        // Auto-Resume Banner with "Start from Beginning" option (Requirement 26)
        AnimatedVisibility(
            visible = autoResumedFromMs != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 76.dp)
        ) {
            if (autoResumedFromMs != null) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xEE1E293B),
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                    modifier = Modifier.testTag("auto_resume_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resumed from ${formatTime(autoResumedFromMs!!)}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { playbackManager.startFromBeginning() },
                            colors = ButtonDefaults.buttonColors(containerColor = VideonicPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("start_from_beginning_button")
                        ) {
                            Text("START FROM BEGINNING", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { playbackManager.clearAutoResumeBanner() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }



        if (showDecoderDialog) {
            DialogDecoderSelector(
                currentMode = decoderMode,
                onDismiss = { showDecoderDialog = false },
                onApplyDecoder = { mode ->
                    playbackManager.setDecoderMode(mode)
                }
            )
        }

        if (showEqualizerDialog) {
            DialogEqualizer(
                equalizerController = playbackManager.equalizerController,
                onDismiss = { showEqualizerDialog = false }
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

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

fun formatTime(ms: Long): String {
    if (ms <= 0) return "00:00"
    val totalSec = ms / 1000
    val sec = totalSec % 60
    val min = (totalSec / 60) % 60
    val hr = totalSec / 3600
    return if (hr > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hr, min, sec)
    } else {
        String.format(Locale.US, "%02d:%02d", min, sec)
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerSurface(
    playbackManager: PlaybackManager,
    aspectRatioMode: AspectRatioMode = AspectRatioMode.FIT,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = playbackManager.exoPlayer
                useController = false
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { playerView ->
            playerView.player = playbackManager.exoPlayer
            playerView.resizeMode = when (aspectRatioMode) {
                AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                AspectRatioMode.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                AspectRatioMode.SIXTEEN_NINE -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                AspectRatioMode.FOUR_THREE -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
            }
        },
        modifier = modifier
    )
}

@Composable
fun VerticalSwipeGestureControl(
    type: GestureType,
    progress: Float,
    valueLabel: String,
    isInteractive: Boolean,
    onModeToggle: () -> Unit,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val isBrightness = type == GestureType.BRIGHTNESS
    val accentColor = if (isBrightness) Color(0xFFFBBF24) else VideonicPrimary
    val trackGradient = if (isBrightness) {
        Brush.verticalGradient(listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF38BDF8), VideonicPrimary))
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xF00F172A),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = if (isInteractive) 0.85f else 0.4f)),
        shadowElevation = 8.dp,
        modifier = modifier
            .width(60.dp)
            .testTag("left_vertical_swipe_control")
            .pointerInput(type, progress) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    val delta = -dragAmount / 110.dp.toPx()
                    val newProgress = (progress + delta).coerceIn(0.01f, 1f)
                    onProgressChange(newProgress)
                }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 6.dp)
        ) {
            // Mode toggle icon button
            IconButton(
                onClick = onModeToggle,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .testTag("vertical_control_toggle_button")
            ) {
                Icon(
                    imageVector = if (isBrightness) Icons.Default.BrightnessMedium else Icons.Default.VolumeUp,
                    contentDescription = if (isBrightness) "Screen Brightness" else "Volume",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Value label
            Text(
                text = valueLabel,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            // Vertical Progress Slider Track
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .height(110.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((110 * progress.coerceIn(0.02f, 1f)).dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(trackGradient)
                )
            }

            // Mode indicator text
            Text(
                text = if (isBrightness) "BRIGHT" else "VOLUME",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

