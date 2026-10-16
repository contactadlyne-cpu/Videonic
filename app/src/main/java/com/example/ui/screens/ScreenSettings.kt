package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.ViewLayout
import com.example.ui.theme.VideonicPrimary
import com.example.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenSettings(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onOpenThemeDialog: () -> Unit,
    onRefreshLibrary: () -> Unit,
    onClearHistory: () -> Unit,
    onAboutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val viewLayout by viewModel.viewLayout.collectAsState()
    val showThumbnails by viewModel.showThumbnails.collectAsState()
    val resumePlayback by viewModel.resumePlayback.collectAsState()
    val autoPlayNext by viewModel.autoPlayNext.collectAsState()
    val defaultSpeed by viewModel.defaultSpeed.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val autoRotateVideo by viewModel.autoRotateVideo.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        val padding = PaddingValues(0.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .testTag("screen_settings")
        ) {
            // Group: Appearance
            SettingsGroupHeader("Appearance")

            val themeLabel = when (themeMode) {
                ThemeMode.LIGHT -> "Light"
                ThemeMode.DARK -> "Dark"
                ThemeMode.SYSTEM -> "System default"
            }
            SettingsNavRow(
                icon = Icons.Default.DarkMode,
                title = "Theme",
                subtitle = themeLabel,
                onClick = onOpenThemeDialog
            )

            SettingsNavRow(
                icon = Icons.Default.GridView,
                title = "Default View",
                subtitle = if (viewLayout == ViewLayout.GRID) "Grid" else "List",
                onClick = {
                    viewModel.setViewLayout(if (viewLayout == ViewLayout.GRID) ViewLayout.LIST else ViewLayout.GRID)
                }
            )

            SettingsToggleRow(
                icon = Icons.Default.Image,
                title = "Show Thumbnails",
                checked = showThumbnails,
                onCheckedChange = { viewModel.setShowThumbnails(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Group: Playback
            SettingsGroupHeader("Playback")

            SettingsToggleRow(
                icon = Icons.Default.Replay,
                title = "Resume Playback",
                checked = resumePlayback,
                onCheckedChange = { viewModel.setResumePlayback(it) }
            )

            SettingsToggleRow(
                icon = Icons.Default.PlayCircle,
                title = "Auto Play Next",
                checked = autoPlayNext,
                onCheckedChange = { viewModel.setAutoPlayNext(it) }
            )



            SettingsNavRow(
                icon = Icons.Default.Speed,
                title = "Default Playback Speed",
                subtitle = "${defaultSpeed}x",
                onClick = {
                    val nextSpeed = when (defaultSpeed) {
                        1.0f -> 1.25f
                        1.25f -> 1.5f
                        1.5f -> 2.0f
                        2.0f -> 0.75f
                        else -> 1.0f
                    }
                    viewModel.setDefaultSpeed(nextSpeed)
                }
            )

            SettingsToggleRow(
                icon = Icons.Default.BrightnessAuto,
                title = "Screen-on While Playing",
                checked = keepScreenOn,
                onCheckedChange = { viewModel.setKeepScreenOn(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Group: Library
            SettingsGroupHeader("Library")

            var showClearHistoryDialog by remember { mutableStateOf(false) }

            SettingsNavRow(
                icon = Icons.Default.DeleteSweep,
                title = "Clear Watch History",
                subtitle = "Reset all saved playback progress",
                onClick = { showClearHistoryDialog = true }
            )

            if (showClearHistoryDialog) {
                AlertDialog(
                    onDismissRequest = { showClearHistoryDialog = false },
                    title = { Text("Clear Watch History", fontWeight = FontWeight.Bold) },
                    text = { Text("Are you sure you want to clear your entire watch history? All saved playback progress will be reset.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showClearHistoryDialog = false
                                onClearHistory()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("confirm_clear_history_settings")
                        ) {
                            Text("Clear")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearHistoryDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = VideonicPrimary,
            fontSize = 13.sp
        ),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        com.example.ui.components.VideonicSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
