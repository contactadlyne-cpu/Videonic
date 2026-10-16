package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "videonic_settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class ViewLayout {
    GRID, LIST
}

enum class VideoSortOrder {
    DATE_DESC, DATE_ASC, NAME_ASC, NAME_DESC, SIZE_DESC, DURATION_DESC
}

enum class AspectRatioMode(val label: String) {
    FIT("Fit"),
    FILL("Fill"),
    ZOOM("Zoom"),
    ORIGINAL("Original"),
    SIXTEEN_NINE("16:9"),
    FOUR_THREE("4:3")
}

enum class AppColorTheme(val label: String, val primaryColorHex: Long) {
    BLUE("Videonic Blue", 0xFF2563EB),
    PURPLE("Cyber Purple", 0xFF8B5CF6),
    EMERALD("Emerald Green", 0xFF10B981),
    SUNSET("Sunset Amber", 0xFFF59E0B),
    CRIMSON("Crimson Red", 0xFFEF4444)
}

class PreferencesManager(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val APP_COLOR_THEME = stringPreferencesKey("app_color_theme")
        val DEFAULT_VIEW = stringPreferencesKey("default_view")
        val SHOW_THUMBNAILS = booleanPreferencesKey("show_thumbnails")
        val RESUME_PLAYBACK = booleanPreferencesKey("resume_playback")
        val AUTO_PLAY_NEXT = booleanPreferencesKey("auto_play_next")
        val DEFAULT_SPEED = floatPreferencesKey("default_speed")
        val DEFAULT_ASPECT = stringPreferencesKey("default_aspect")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val AUTO_ROTATE_VIDEO = booleanPreferencesKey("auto_rotate_video")
        val SEEK_INTERVAL_SEC = intPreferencesKey("seek_interval_sec")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val HAS_SEEN_PERMISSION_PRIMER = booleanPreferencesKey("has_seen_permission_primer")
        val PRIVATE_FOLDER_PIN = stringPreferencesKey("private_folder_pin")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val value = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        runCatching { ThemeMode.valueOf(value) }.getOrDefault(ThemeMode.SYSTEM)
    }

    val appColorTheme: Flow<AppColorTheme> = context.dataStore.data.map { prefs ->
        val value = prefs[Keys.APP_COLOR_THEME] ?: AppColorTheme.BLUE.name
        runCatching { AppColorTheme.valueOf(value) }.getOrDefault(AppColorTheme.BLUE)
    }

    val privateFolderPin: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.PRIVATE_FOLDER_PIN]
    }

    val viewLayout: Flow<ViewLayout> = context.dataStore.data.map { prefs ->
        val value = prefs[Keys.DEFAULT_VIEW] ?: ViewLayout.LIST.name
        runCatching { ViewLayout.valueOf(value) }.getOrDefault(ViewLayout.LIST)
    }

    val showThumbnails: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SHOW_THUMBNAILS] ?: true
    }

    val resumePlayback: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.RESUME_PLAYBACK] ?: true
    }

    val autoPlayNext: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.AUTO_PLAY_NEXT] ?: true
    }

    val defaultSpeed: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_SPEED] ?: 1.0f
    }

    val defaultAspect: Flow<AspectRatioMode> = context.dataStore.data.map { prefs ->
        val value = prefs[Keys.DEFAULT_ASPECT] ?: AspectRatioMode.FIT.name
        runCatching { AspectRatioMode.valueOf(value) }.getOrDefault(AspectRatioMode.FIT)
    }

    val keepScreenOn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.KEEP_SCREEN_ON] ?: true
    }

    val autoRotateVideo: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.AUTO_ROTATE_VIDEO] ?: false
    }

    val seekIntervalSeconds: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.SEEK_INTERVAL_SEC] ?: 10
    }

    val sortOrder: Flow<VideoSortOrder> = context.dataStore.data.map { prefs ->
        val value = prefs[Keys.SORT_ORDER] ?: VideoSortOrder.DATE_DESC.name
        runCatching { VideoSortOrder.valueOf(value) }.getOrDefault(VideoSortOrder.DATE_DESC)
    }

    val hasSeenPermissionPrimer: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.HAS_SEEN_PERMISSION_PRIMER] ?: false
    }


    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setViewLayout(layout: ViewLayout) {
        context.dataStore.edit { it[Keys.DEFAULT_VIEW] = layout.name }
    }

    suspend fun setShowThumbnails(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_THUMBNAILS] = show }
    }

    suspend fun setResumePlayback(resume: Boolean) {
        context.dataStore.edit { it[Keys.RESUME_PLAYBACK] = resume }
    }

    suspend fun setAutoPlayNext(autoPlay: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_PLAY_NEXT] = autoPlay }
    }

    suspend fun setDefaultSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.DEFAULT_SPEED] = speed }
    }

    suspend fun setDefaultAspect(aspect: AspectRatioMode) {
        context.dataStore.edit { it[Keys.DEFAULT_ASPECT] = aspect.name }
    }

    suspend fun setKeepScreenOn(keep: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_SCREEN_ON] = keep }
    }

    suspend fun setAutoRotateVideo(autoRotate: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_ROTATE_VIDEO] = autoRotate }
    }

    suspend fun setSeekIntervalSeconds(seconds: Int) {
        context.dataStore.edit { it[Keys.SEEK_INTERVAL_SEC] = seconds }
    }

    suspend fun setSortOrder(order: VideoSortOrder) {
        context.dataStore.edit { it[Keys.SORT_ORDER] = order.name }
    }

    suspend fun setHasSeenPermissionPrimer(seen: Boolean) {
        context.dataStore.edit { it[Keys.HAS_SEEN_PERMISSION_PRIMER] = seen }
    }

    suspend fun setAppColorTheme(theme: AppColorTheme) {
        context.dataStore.edit { it[Keys.APP_COLOR_THEME] = theme.name }
    }

    suspend fun setPrivateFolderPin(pin: String?) {
        context.dataStore.edit {
            if (pin == null) {
                it.remove(Keys.PRIVATE_FOLDER_PIN)
            } else {
                it[Keys.PRIVATE_FOLDER_PIN] = pin
            }
        }
    }
}
