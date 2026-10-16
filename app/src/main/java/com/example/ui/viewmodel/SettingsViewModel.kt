package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.AspectRatioMode
import com.example.data.preferences.PreferencesManager
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.VideoSortOrder
import com.example.data.preferences.ViewLayout
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)

    val themeMode: StateFlow<ThemeMode> = preferencesManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val appColorTheme: StateFlow<com.example.data.preferences.AppColorTheme> = preferencesManager.appColorTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.preferences.AppColorTheme.BLUE)

    val viewLayout: StateFlow<ViewLayout> = preferencesManager.viewLayout
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ViewLayout.GRID)

    val showThumbnails: StateFlow<Boolean> = preferencesManager.showThumbnails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val resumePlayback: StateFlow<Boolean> = preferencesManager.resumePlayback
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoPlayNext: StateFlow<Boolean> = preferencesManager.autoPlayNext
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val defaultSpeed: StateFlow<Float> = preferencesManager.defaultSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    val defaultAspect: StateFlow<AspectRatioMode> = preferencesManager.defaultAspect
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AspectRatioMode.FIT)

    val keepScreenOn: StateFlow<Boolean> = preferencesManager.keepScreenOn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoRotateVideo: StateFlow<Boolean> = preferencesManager.autoRotateVideo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val seekIntervalSeconds: StateFlow<Int> = preferencesManager.seekIntervalSeconds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)

    val hasSeenPermissionPrimer: StateFlow<Boolean> = preferencesManager.hasSeenPermissionPrimer
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesManager.setThemeMode(mode) }
    }

    fun setAppColorTheme(theme: com.example.data.preferences.AppColorTheme) {
        viewModelScope.launch { preferencesManager.setAppColorTheme(theme) }
    }

    fun setViewLayout(layout: ViewLayout) {
        viewModelScope.launch { preferencesManager.setViewLayout(layout) }
    }

    fun setShowThumbnails(show: Boolean) {
        viewModelScope.launch { preferencesManager.setShowThumbnails(show) }
    }

    fun setResumePlayback(resume: Boolean) {
        viewModelScope.launch { preferencesManager.setResumePlayback(resume) }
    }

    fun setAutoPlayNext(autoPlay: Boolean) {
        viewModelScope.launch { preferencesManager.setAutoPlayNext(autoPlay) }
    }

    fun setDefaultSpeed(speed: Float) {
        viewModelScope.launch { preferencesManager.setDefaultSpeed(speed) }
    }

    fun setDefaultAspect(aspect: AspectRatioMode) {
        viewModelScope.launch { preferencesManager.setDefaultAspect(aspect) }
    }

    fun setKeepScreenOn(keep: Boolean) {
        viewModelScope.launch { preferencesManager.setKeepScreenOn(keep) }
    }

    fun setAutoRotateVideo(autoRotate: Boolean) {
        viewModelScope.launch { preferencesManager.setAutoRotateVideo(autoRotate) }
    }

    fun setSeekIntervalSeconds(seconds: Int) {
        viewModelScope.launch { preferencesManager.setSeekIntervalSeconds(seconds) }
    }

    fun setHasSeenPermissionPrimer(seen: Boolean) {
        viewModelScope.launch { preferencesManager.setHasSeenPermissionPrimer(seen) }
    }

}
