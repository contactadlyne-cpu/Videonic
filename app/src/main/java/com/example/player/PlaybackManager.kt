package com.example.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.model.VideoItem
import com.example.data.preferences.AspectRatioMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlaybackManager(private val context: Context) {
private var mediaSession: MediaSession? = null
private val _isBackgroundAudioEnabled = MutableStateFlow(false)
val isBackgroundAudioEnabled: StateFlow<Boolean> = _isBackgroundAudioEnabled.asStateFlow()
private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
private val scope = CoroutineScope(Dispatchers.Main + Job())
val equalizerController = EqualizerController()
val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
.setAudioAttributes(
AudioAttributes.Builder()
.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
.setUsage(C.USAGE_MEDIA)
.build(),
true
)
.setHandleAudioBecomingNoisy(true)
.setSeekBackIncrementMs(10000)
.setSeekForwardIncrementMs(10000)
.build()
private val _currentVideo = MutableStateFlow<VideoItem?>(null)
val currentVideo: StateFlow<VideoItem?> = _currentVideo.asStateFlow()
private val _isPlaying = MutableStateFlow(false)
val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
private val _isEnded = MutableStateFlow(false)
val isEnded: StateFlow<Boolean> = _isEnded.asStateFlow()
private val _currentPositionMs = MutableStateFlow(0L)
val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()
private val _durationMs = MutableStateFlow(0L)
val durationMs: StateFlow<Long> = _durationMs.asStateFlow()
private val _playbackSpeed = MutableStateFlow(1.0f)
val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()
private val _decoderMode = MutableStateFlow(DecoderMode.HW)
val decoderMode: StateFlow<DecoderMode> = _decoderMode.asStateFlow()
private val _aspectRatioMode = MutableStateFlow(AspectRatioMode.FIT)
val aspectRatioMode: StateFlow<AspectRatioMode> = _aspectRatioMode.asStateFlow()
private val _isRepeatOne = MutableStateFlow(false)
val isRepeatOne: StateFlow<Boolean> = _isRepeatOne.asStateFlow()
private val _isLocked = MutableStateFlow(false)
val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()
private val _audioTracks = MutableStateFlow<List<TrackItem>>(emptyList())
val audioTracks: StateFlow<List<TrackItem>> = _audioTracks.asStateFlow()
private val _subtitleTracks = MutableStateFlow<List<TrackItem>>(emptyList())
val subtitleTracks: StateFlow<List<TrackItem>> = _subtitleTracks.asStateFlow()
private val _sleepTimerSeconds = MutableStateFlow(0)
val sleepTimerSeconds: StateFlow<Int> = _sleepTimerSeconds.asStateFlow()
private val _autoPlayNext = MutableStateFlow(true)
val autoPlayNext: StateFlow<Boolean> = _autoPlayNext.asStateFlow()
private val _isSpeedBoostActive = MutableStateFlow(false)
val isSpeedBoostActive: StateFlow<Boolean> = _isSpeedBoostActive.asStateFlow()
// Holds position if video was auto-resumed so UI can show "Start from Beginning"
private val _autoResumedFromMs = MutableStateFlow<Long?>(null)
val autoResumedFromMs: StateFlow<Long?> = _autoResumedFromMs.asStateFlow()
private var speedBeforeBoost: Float = 1.0f
private var currentPlaylist: List<VideoItem> = emptyList()
private var currentPlaylistIndex: Int = -1
var libraryVideosSupplier: (() -> List<VideoItem>)? = null
private var positionUpdateJob: Job? = null
private var sleepTimerJob: Job? = null
// For smooth, crash-proof rapid 10s seek
private var pendingSeekTargetMs: Long? = null
private var seekDebounceJob: Job? = null
private val playerListener = object : Player.Listener {
override fun onIsPlayingChanged(isPlaying: Boolean) {
_isPlaying.value = isPlaying
if (isPlaying) {
_isEnded.value = false
startPositionTracker()
} else {
stopPositionTracker()
}
if (_isBackgroundAudioEnabled.value) {
updateMediaNotification()
}
}
override fun onPlaybackStateChanged(playbackState: Int) {
if (playbackState == Player.STATE_READY) {
_durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
_isEnded.value = false
try {
val sessionId = exoPlayer.audioSessionId
if (sessionId != C.AUDIO_SESSION_ID_UNSET && sessionId > 0) {
equalizerController.attachAudioSession(sessionId)
}
} catch (e: Exception) {
// Ignore
}
} else if (playbackState == Player.STATE_ENDED) {
_isEnded.value = true
_isPlaying.value = false
_currentPositionMs.value = exoPlayer.duration.coerceAtLeast(0L)
if (_isRepeatOne.value) {
restartPlayback()
} else if (_autoPlayNext.value) {
scope.launch {
delay(250)
val advanced = playNextVideo()
if (!advanced) {
val fallback = libraryVideosSupplier?.invoke() ?: emptyList()
val curVid = _currentVideo.value
val curIdx = fallback.indexOfFirst {
it.id == curVid?.id || it.uri.toString() == curVid?.uri.toString()
}
if (curIdx >= 0 && curIdx + 1 < fallback.size) {
playVideo(fallback[curIdx + 1], 0L, fallback)
}
}
}
}
}
}
override fun onTracksChanged(tracks: Tracks) {
updateAvailableTracks(tracks)
}
}
init {
    exoPlayer.addListener(playerListener)
    try {
        mediaSession = MediaSession.Builder(context, exoPlayer).build()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
fun playVideo(video: VideoItem, startPositionMs: Long = 0L, playlist: List<VideoItem> = emptyList()) {
_currentVideo.value = video
_isEnded.value = false
if (playlist.isNotEmpty()) {
currentPlaylist = playlist
val idx = playlist.indexOfFirst {
it.id == video.id || it.uri.toString() == video.uri.toString()
}
currentPlaylistIndex = if (idx >= 0) idx else 0
} else {
val fallback = libraryVideosSupplier?.invoke() ?: emptyList()
val foundIdx = fallback.indexOfFirst {
it.id == video.id || it.uri.toString() == video.uri.toString()
}
if (foundIdx >= 0) {
currentPlaylist = fallback
currentPlaylistIndex = foundIdx
} else {
currentPlaylist = listOf(video)
currentPlaylistIndex = 0
}
}
if (startPositionMs > 3000L) {
_autoResumedFromMs.value = startPositionMs
} else {
_autoResumedFromMs.value = null
}
val mediaItem = MediaItem.fromUri(video.uri)
_playbackSpeed.value = 1.0f
exoPlayer.playbackParameters = PlaybackParameters(1.0f)
exoPlayer.stop()
exoPlayer.clearMediaItems()
exoPlayer.setMediaItem(mediaItem)
exoPlayer.prepare()
if (startPositionMs > 0) {
exoPlayer.seekTo(startPositionMs)
}
exoPlayer.playWhenReady = true
exoPlayer.play()
_currentPositionMs.value = startPositionMs
_isPlaying.value = true
startPositionTracker()
}
fun clearAutoResumeBanner() {
_autoResumedFromMs.value = null
}
fun restartPlayback() {
_isEnded.value = false
exoPlayer.stop()
exoPlayer.seekTo(0, 0L)
exoPlayer.prepare()
exoPlayer.playWhenReady = true
exoPlayer.play()
_isPlaying.value = true
_currentPositionMs.value = 0L
startPositionTracker()
}
fun startFromBeginning() {
clearAutoResumeBanner()
restartPlayback()
}
fun setDecoderMode(mode: DecoderMode) {
if (_decoderMode.value == mode) return
_decoderMode.value = mode
// Preserve current playback state and position
val curPos = exoPlayer.currentPosition
val curVideo = _currentVideo.value ?: return
val wasPlaying = exoPlayer.isPlaying
playVideo(curVideo, curPos, currentPlaylist)
if (!wasPlaying) {
exoPlayer.pause()
}
}
fun toggleDecoderMode(): DecoderMode {
val next = if (_decoderMode.value == DecoderMode.HW) DecoderMode.SW else DecoderMode.HW
setDecoderMode(next)
return next
}
fun playNextVideo(): Boolean {
if (currentPlaylistIndex >= 0 && currentPlaylistIndex + 1 < currentPlaylist.size) {
val nextVideo = currentPlaylist[currentPlaylistIndex + 1]
playVideo(nextVideo, 0L, currentPlaylist)
return true
}
return false
}
fun playPreviousVideo(): Boolean {
if (currentPlaylistIndex > 0 && currentPlaylistIndex < currentPlaylist.size) {
val prevVideo = currentPlaylist[currentPlaylistIndex - 1]
playVideo(prevVideo, 0L, currentPlaylist)
return true
}
return false
}
fun setAutoPlayNext(enabled: Boolean) {
_autoPlayNext.value = enabled
}
fun setTemporarySpeedBoost(active: Boolean, boostSpeed: Float = 2.0f) {
if (active) {
if (!_isSpeedBoostActive.value) {
speedBeforeBoost = _playbackSpeed.value
_isSpeedBoostActive.value = true
}
exoPlayer.playbackParameters = PlaybackParameters(boostSpeed)
} else {
if (_isSpeedBoostActive.value) {
_isSpeedBoostActive.value = false
exoPlayer.playbackParameters = PlaybackParameters(speedBeforeBoost)
}
}
}
fun cycleAspectRatio(): AspectRatioMode {
val next = when (_aspectRatioMode.value) {
AspectRatioMode.FIT -> AspectRatioMode.FILL
AspectRatioMode.FILL -> AspectRatioMode.ZOOM
AspectRatioMode.ZOOM -> AspectRatioMode.ORIGINAL
AspectRatioMode.ORIGINAL -> AspectRatioMode.SIXTEEN_NINE
AspectRatioMode.SIXTEEN_NINE -> AspectRatioMode.FOUR_THREE
AspectRatioMode.FOUR_THREE -> AspectRatioMode.FIT
}
_aspectRatioMode.value = next
return next
}
fun playUri(uri: Uri, title: String) {
val video = VideoItem(
id = 0,
uri = uri,
title = title,
displayName = title,
durationMs = 0L,
sizeBytes = 0L,
width = 0,
height = 0,
mimeType = "video/*",
dateAdded = System.currentTimeMillis() / 1000,
folderName = "External",
path = uri.toString()
)
playVideo(video)
}
fun togglePlayPause() {
if (_isEnded.value) {
restartPlayback()
return
}
if (exoPlayer.isPlaying) {
exoPlayer.pause()
} else {
exoPlayer.play()
}
}
fun pausePlayback() {
exoPlayer.pause()
_isPlaying.value = false
stopPositionTracker()
}
fun stopPlayback() {
exoPlayer.stop()
_isPlaying.value = false
stopPositionTracker()
}
fun seekTo(positionMs: Long) {
val safePos = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(0L))
pendingSeekTargetMs = safePos
exoPlayer.seekTo(safePos)
_currentPositionMs.value = safePos
_isEnded.value = false
}
/**
* Resilient relative seek (+/- ms) that prevents rapid clicks from stopping playback.
* Accumulates offsets and avoids triggering Player.STATE_ENDED abruptly.
*/
fun seekRelative(offsetMs: Long) {
val duration = _durationMs.value.coerceAtLeast(0L)
val basePos = pendingSeekTargetMs ?: exoPlayer.currentPosition
// If duration is known, leave a 1.2s buffer at the end so seek doesn't trigger STATE_ENDED and stop
val maxSafeTarget = if (duration > 3000L) duration - 1200L else duration
val target = (basePos + offsetMs).coerceIn(0L, maxSafeTarget)
pendingSeekTargetMs = target
_currentPositionMs.value = target
_isEnded.value = false
seekDebounceJob?.cancel()
seekDebounceJob = scope.launch {
delay(120) // accumulate rapid consecutive clicks seamlessly
exoPlayer.seekTo(target)
if (!exoPlayer.isPlaying && exoPlayer.playbackState != Player.STATE_ENDED) {
exoPlayer.play()
}
delay(300)
pendingSeekTargetMs = null
}
}
fun setSpeed(speed: Float) {
val clamped = speed.coerceIn(0.25f, 4.0f)
_playbackSpeed.value = clamped
exoPlayer.playbackParameters = PlaybackParameters(clamped)
}
fun setAspectRatio(mode: AspectRatioMode) {
_aspectRatioMode.value = mode
}
fun toggleRepeatOne() {
_isRepeatOne.value = !_isRepeatOne.value
}
fun setLocked(locked: Boolean) {
_isLocked.value = locked
}
fun selectAudioTrack(trackItem: TrackItem) {
val currentTracks = exoPlayer.currentTracks
for (group in currentTracks.groups) {
if (group.type == C.TRACK_TYPE_AUDIO) {
for (i in 0 until group.length) {
val format = group.getTrackFormat(i)
if (format.id == trackItem.id || format.label == trackItem.name) {
exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
.buildUpon()
.setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, listOf(i)))
.build()
break
}
}
}
}
}
fun selectSubtitleTrack(trackItem: TrackItem?) {
if (trackItem == null) {
exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
.buildUpon()
.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
.build()
} else {
val currentTracks = exoPlayer.currentTracks
for (group in currentTracks.groups) {
if (group.type == C.TRACK_TYPE_TEXT) {
for (i in 0 until group.length) {
val format = group.getTrackFormat(i)
if (format.id == trackItem.id || format.label == trackItem.name) {
exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
.buildUpon()
.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
.setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, listOf(i)))
.build()
break
}
}
}
}
}
}
fun setSleepTimer(preset: SleepTimerPreset, customMinutes: Int = 0) {
sleepTimerJob?.cancel()
val totalMinutes = when (preset) {
SleepTimerPreset.OFF -> 0
SleepTimerPreset.M15 -> 15
SleepTimerPreset.M30 -> 30
SleepTimerPreset.H1 -> 60
SleepTimerPreset.H2 -> 120
SleepTimerPreset.CUSTOM -> customMinutes
SleepTimerPreset.END_OF_VIDEO -> {
val remainingMs = _durationMs.value - _currentPositionMs.value
val remainingSec = (remainingMs / 1000).toInt().coerceAtLeast(1)
startSleepTimerCountdown(remainingSec)
return
}
}
if (totalMinutes > 0) {
startSleepTimerCountdown(totalMinutes * 60)
} else {
_sleepTimerSeconds.value = 0
}
}
private fun startSleepTimerCountdown(seconds: Int) {
_sleepTimerSeconds.value = seconds
sleepTimerJob = scope.launch {
var remaining = seconds
while (isActive && remaining > 0) {
delay(1000)
remaining--
_sleepTimerSeconds.value = remaining
}
if (isActive && remaining <= 0) {
exoPlayer.pause()
_sleepTimerSeconds.value = 0
}
}
}
fun cancelSleepTimer() {
sleepTimerJob?.cancel()
_sleepTimerSeconds.value = 0
}
fun getMaxVolume(): Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
fun getCurrentVolume(): Int = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
fun setVolume(volume: Int) {
val max = getMaxVolume()
val safeVol = volume.coerceIn(0, max)
audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, safeVol, 0)
}
private fun updateAvailableTracks(tracks: Tracks) {
val audios = mutableListOf<TrackItem>()
val subs = mutableListOf<TrackItem>()
for (group in tracks.groups) {
if (group.type == C.TRACK_TYPE_AUDIO) {
for (i in 0 until group.length) {
val format = group.getTrackFormat(i)
val name = format.label ?: format.language ?: "Audio Track ${audios.size + 1}"
audios.add(
TrackItem(
id = format.id ?: "$i",
name = name,
language = format.language,
isSelected = group.isTrackSelected(i)
)
)
}
} else if (group.type == C.TRACK_TYPE_TEXT) {
for (i in 0 until group.length) {
val format = group.getTrackFormat(i)
val name = format.label ?: format.language ?: "Subtitle ${subs.size + 1}"
subs.add(
TrackItem(
id = format.id ?: "$i",
name = name,
language = format.language,
isSelected = group.isTrackSelected(i)
)
)
}
}
}
_audioTracks.value = audios
_subtitleTracks.value = subs
}
private fun startPositionTracker() {
positionUpdateJob?.cancel()
positionUpdateJob = scope.launch {
while (isActive) {
if (pendingSeekTargetMs == null) {
_currentPositionMs.value = exoPlayer.currentPosition.coerceAtLeast(0L)
}
_durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
delay(400)
}
}
}
private fun stopPositionTracker() {
positionUpdateJob?.cancel()
if (pendingSeekTargetMs == null) {
_currentPositionMs.value = exoPlayer.currentPosition.coerceAtLeast(0L)
}
}
fun onPause(isFinishing: Boolean, isInPip: Boolean) {
    if (!isFinishing && !isInPip && !_isBackgroundAudioEnabled.value) {
        exoPlayer.pause()
    } else if (_isBackgroundAudioEnabled.value) {
        updateMediaNotification()
    }
}
fun release() {
    positionUpdateJob?.cancel()
    sleepTimerJob?.cancel()
    seekDebounceJob?.cancel()
    equalizerController.release()
    exoPlayer.removeListener(playerListener)
    try {
        mediaSession?.release()
        mediaSession = null
    } catch (e: Exception) {
        e.printStackTrace()
    }
    exoPlayer.release()
}
fun toggleBackgroundAudio(): Boolean {
    val next = !_isBackgroundAudioEnabled.value
    _isBackgroundAudioEnabled.value = next
    if (next) {
        updateMediaNotification()
    } else {
        removeMediaNotification()
    }
    return next
}
fun setBackgroundAudioEnabled(enabled: Boolean) {
    _isBackgroundAudioEnabled.value = enabled
    if (enabled) {
        updateMediaNotification()
    } else {
        removeMediaNotification()
    }
}
fun updateMediaNotification() {
    if (!_isBackgroundAudioEnabled.value) return
    val curVideo = _currentVideo.value ?: return
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "videonic_bg_audio",
            "Background Audio Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background video audio playback controls"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    val openAppIntent = Intent(context, com.example.MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        0,
        openAppIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val prevIntent = PendingIntent.getBroadcast(
        context,
        201,
        Intent("com.example.ACTION_MEDIA_PREV").setPackage(context.packageName),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val playPauseIntent = PendingIntent.getBroadcast(
        context,
        202,
        Intent("com.example.ACTION_MEDIA_PLAY_PAUSE").setPackage(context.packageName),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val nextIntent = PendingIntent.getBroadcast(
        context,
        203,
        Intent("com.example.ACTION_MEDIA_NEXT").setPackage(context.packageName),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val stopIntent = PendingIntent.getBroadcast(
        context,
        204,
        Intent("com.example.ACTION_MEDIA_STOP").setPackage(context.packageName),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val isPlaying = _isPlaying.value
    val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
    val playPauseTitle = if (isPlaying) "Pause" else "Play"

    val builder = NotificationCompat.Builder(context, "videonic_bg_audio")
        .setSmallIcon(com.example.R.mipmap.ic_launcher)
        .setContentTitle(curVideo.displayName)
        .setContentText(if (isPlaying) "Playing in background" else "Paused in background")
        .setContentIntent(contentPendingIntent)
        .setOngoing(isPlaying)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .addAction(android.R.drawable.ic_media_previous, "Previous", prevIntent)
        .addAction(playPauseIcon, playPauseTitle, playPauseIntent)
        .addAction(android.R.drawable.ic_media_next, "Next", nextIntent)
        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)

    val session = mediaSession
    if (session != null) {
        builder.setStyle(MediaStyleNotificationHelper.MediaStyle(session))
    }

    try {
        notificationManager.notify(2002, builder.build())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
fun removeMediaNotification() {
val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
try {
notificationManager.cancel(2002)
} catch (e: Exception) {
e.printStackTrace()
}
}
}